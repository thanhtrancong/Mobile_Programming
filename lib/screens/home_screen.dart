// screens/home_screen.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../db/database_helper.dart';
import '../models/transaction.dart';
import 'add_transaction_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final dbHelper = DatabaseHelper();
  // Future chứa danh sách giao dịch (Map)
  late Future<List<Map<String, dynamic>>> _transactionsFuture;
  // Future chứa tổng chi
  late Future<double> _totalExpenseFuture;
  // Future chứa tổng thu
  late Future<double> _totalIncomeFuture;

  @override
  void initState() {
    super.initState();
    _refreshData(); // Tải dữ liệu khi màn hình khởi tạo
  }

  // Phương thức tải lại toàn bộ dữ liệu (cần gọi sau mỗi thao tác CREATE/UPDATE/DELETE)
  void _refreshData() {
    setState(() {
      _transactionsFuture = dbHelper.getTransactionsWithCategory();
      _totalExpenseFuture = dbHelper.calculateTotal(0); // 0 là Chi tiêu
      _totalIncomeFuture = dbHelper.calculateTotal(1); // 1 là Thu nhập
    });
  }

  // Mở màn hình thêm giao dịch (Sinh viên tự triển khai màn hình này)
  void _openAddTransactionScreen() async {
    final width = MediaQuery.of(context).size.width;
    bool? result;
    if (width >= 700) {
      // Show as animated dialog on wide screens
      result = await showGeneralDialog<bool>(
        context: context,
        barrierDismissible: true,
        barrierLabel: 'AddTransaction',
        transitionDuration: const Duration(milliseconds: 300),
        pageBuilder: (ctx, anim1, anim2) {
          return Center(
            child: Material(
              color: Colors.transparent,
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 700),
                child: SizedBox(height: 600, child: AddTransactionScreen()),
              ),
            ),
          );
        },
        transitionBuilder: (ctx, anim1, anim2, child) {
          final curved = Curves.easeOut.transform(anim1.value);
          return Transform.scale(scale: curved, child: Opacity(opacity: anim1.value, child: child));
        },
      );
    } else {
      result = await Navigator.push(context, MaterialPageRoute(builder: (c) => AddTransactionScreen()));
    }
    if (result == true) _refreshData();
  }

  @override
  Widget build(BuildContext context) {
    // Use LayoutBuilder to adapt UI based on available width
    return Scaffold(
      appBar: AppBar(title: const Text('MyMoney - Quản lý Chi tiêu')),
      body: LayoutBuilder(
        builder: (context, constraints) {
          // Center content and constrain max width for very wide screens
          return Center(
            child: ConstrainedBox(
              constraints: BoxConstraints(maxWidth: 900),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
                child: constraints.maxWidth >= 700
                    ? _buildWideLayout(constraints)
                    : _buildNarrowLayout(),
              ),
            ),
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _openAddTransactionScreen,
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildWideLayout(BoxConstraints constraints) {
    // Two-column layout: left summary, right list
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Flexible(
          flex: 1,
          child: Column(
            children: [
              _buildSummaryCard(isCompact: false),
            ],
          ),
        ),
        const SizedBox(width: 16),
        Flexible(
          flex: 2,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Padding(
                padding: EdgeInsets.only(bottom: 8.0),
                child: Text('Giao dịch Gần nhất', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              ),
              _buildTransactionList(),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildNarrowLayout() {
    return SingleChildScrollView(
      child: Column(
        children: [
          _buildSummaryCard(isCompact: true),
          const Padding(
            padding: EdgeInsets.all(16.0),
            child: Text('Giao dịch Gần nhất', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          ),
          _buildTransactionList(),
        ],
      ),
    );
  }

  // Widget hiển thị tổng quan thu/chi/số dư
  Widget _buildSummaryCard({bool isCompact = true}) {
    final padding = isCompact ? 16.0 : 20.0;
    final titleSize = isCompact ? 16.0 : 18.0;
    final amountSize = isCompact ? 24.0 : 28.0;

    return Card(
      margin: EdgeInsets.all(padding),
      child: Padding(
        padding: EdgeInsets.all(padding),
        child: FutureBuilder<List<double>>(
          // Chờ cả 2 Future (Thu và Chi) hoàn thành
          future: Future.wait([_totalIncomeFuture, _totalExpenseFuture]),
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.waiting) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
              return const Text('Lỗi tải tổng quan');
            }

            final income = snapshot.data![0]; // Tổng Thu
            final expense = snapshot.data![1]; // Tổng Chi
            final balance = income - expense; // Số dư

            final currencyFormat = NumberFormat.currency(locale: 'vi_VN', symbol: '\u20ab');

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Số dư hiện tại:', style: TextStyle(fontSize: titleSize)),
                Text(
                  currencyFormat.format(balance),
                  style: TextStyle(
                    fontSize: amountSize,
                    fontWeight: FontWeight.bold,
                    color: balance >= 0 ? Colors.green : Colors.red,
                  ),
                ),
                const Divider(),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Flexible(child: Text('Tổng Thu tháng: ${currencyFormat.format(income)}', style: TextStyle(color: Colors.green))),
                    const SizedBox(width: 8),
                    Flexible(child: Text('Tổng Chi tháng: ${currencyFormat.format(expense)}', style: TextStyle(color: Colors.red))),
                  ],
                ),
              ],
            );
          },
        ),
      ),
    );
  }

  // Widget hiển thị danh sách giao dịch
  Widget _buildTransactionList() {
    return FutureBuilder<List<Map<String, dynamic>>>(
      future: _transactionsFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        }
        if (!snapshot.hasData || snapshot.data!.isEmpty) {
          return const Center(child: Padding(
            padding: EdgeInsets.all(16.0),
            child: Text('Không có giao dịch nào được ghi lại.'),
          ));
        }

        return ListView.builder(
          shrinkWrap: true, // Quan trọng khi dùng trong SingleChildScrollView
          physics: const NeverScrollableScrollPhysics(),
          // Vô hiệu hóa cuộn cho ListView này
          itemCount: snapshot.data!.length,
          itemBuilder: (context, index) {
            final transactionMap = snapshot.data![index];
            // Sử dụng casting an toàn cho các trường có thể null/kiểu khác
            final dynamic amountRaw = transactionMap[columnAmount];
            final double amount = (amountRaw is num) ? amountRaw.toDouble() : double.tryParse(amountRaw?.toString() ?? '') ?? 0.0;
            final String categoryName = (transactionMap['categoryName'] as String?) ?? 'Không rõ';
            final int type = (transactionMap[columnType] is int) ? (transactionMap[columnType] as int) : ((transactionMap[columnType] is num) ? (transactionMap[columnType] as num).toInt() : 0);
            final String note = (transactionMap[columnNote] as String?) ?? '';
            final DateTime date = transactionMap[columnDate] is String ? DateTime.parse(transactionMap[columnDate] as String) : DateTime.now();

            final isExpense = type == 0;
            final color = isExpense ? Colors.red.shade700 : Colors.green.shade700;
            final sign = isExpense ? '-' : '+';
            final currencyFormat = NumberFormat.currency(
                locale: 'vi_VN', symbol: '\u20ab');

            return Dismissible(
              key: ValueKey(transactionMap[columnTransactionId]?.toString() ?? 'txn_$index'),
              background: Container(color: Colors.red, child: const Icon(Icons.delete, color: Colors.white)),
              direction: DismissDirection.endToStart,
              confirmDismiss: (direction) async {
                // Hiển thị hộp thoại xác nhận trước khi xóa
                final confirm = await showDialog<bool>(
                  context: context,
                  builder: (ctx) => AlertDialog(
                    title: const Text('Xác nhận xóa'),
                    content: const Text('Bạn có chắc chắn muốn xóa giao dịch này không?'),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.of(ctx).pop(false),
                        child: const Text('Hủy'),
                      ),
                      TextButton(
                        onPressed: () => Navigator.of(ctx).pop(true),
                        child: const Text('Xóa', style: TextStyle(color: Colors.red)),
                      ),
                    ],
                  ),
                );
                return confirm == true;
              },
              onDismissed: (direction) async {
                final idRaw = transactionMap[columnTransactionId];
                final int? id = (idRaw is int) ? idRaw : ((idRaw is num) ? idRaw.toInt() : null);
                if (id != null) {
                  await dbHelper.deleteTransaction(id);
                  if (!mounted) return;
                  _refreshData(); // Cập nhật lại UI sau khi xóa
                  // Show snackbar after frame to avoid using context across async gap
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    if (!mounted) return;
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Đã xóa giao dịch')),
                    );
                  });
                }
              },
              child: ListTile(
                leading: Icon(isExpense ?
                Icons.arrow_downward : Icons.arrow_upward, color: color),
                title: Text(categoryName,
                    style: TextStyle(fontWeight: FontWeight.bold)),
                subtitle: Text(note.isEmpty ?
                DateFormat('dd/MM/yyyy').format(date) : '$note - '
                    '${DateFormat('dd/MM/yyyy').format(date)}'),
                trailing: Text(
                  '$sign${currencyFormat.format(amount)}',
                  style: TextStyle(color: color, fontWeight: FontWeight.bold),
                ),
                onTap: () async {
                  // Open edit screen with Transaction constructed from map
                  final txnMap = Map<String, dynamic>.from(transactionMap);
                  // Use Transaction.fromMap from models
                  try {
                    final existing = Transaction.fromMap(txnMap);
                    final width = MediaQuery.of(context).size.width;
                    bool? editResult;
                    if (width >= 700) {
                      editResult = await showDialog<bool>(
                        context: context,
                        builder: (ctx) => Dialog(
                          child: ConstrainedBox(
                            constraints: const BoxConstraints(maxWidth: 700),
                            child: SizedBox(height: 600, child: AddTransactionScreen(transaction: existing)),
                          ),
                        ),
                      );
                    } else {
                      editResult = await Navigator.push(context, MaterialPageRoute(builder: (c) => AddTransactionScreen(transaction: existing)));
                    }
                    if (editResult == true) _refreshData();
                  } catch (e) {
                    // ignore parse errors
                  }
                },
              ),
            );
          },
        );
      },
    );
  }
}
