// screens/home_screen.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../db/database_helper.dart';
import '../models/transaction.dart';

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
      _totalIncomeFuture = dbHelper.calculateTotal(1);  // 1 là Thu nhập
    });
  }

  // Mở màn hình thêm giao dịch (Sinh viên tự triển khai màn hình này)
  void _openAddTransactionScreen() async {
    // Navigator.push(context, MaterialPageRoute(builder: (c) => AddTransactionScreen()));
    // Sau khi thêm thành công, gọi _refreshData()
    // bool? added = await Navigator.push(...)
    // if(added == true) _refreshData();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('MyMoney - Quản lý Chi tiêu')),
      body: SingleChildScrollView(
        child: Column(
          children: [
            // --- 1. Hiển thị Báo cáo Tổng quan ---
            _buildSummaryCard(),

            // --- 2. Hiển thị Danh sách Giao dịch ---
            const Padding(
              padding: EdgeInsets.all(16.0),
              child: Text('Giao dịch Gần nhất', style: TextStyle(fontSize: 18,
                  fontWeight: FontWeight.bold)),
            ),
            _buildTransactionList(),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _openAddTransactionScreen,
        child: const Icon(Icons.add),
      ),
    );
  }

  // Widget hiển thị tổng quan thu/chi/số dư
  Widget _buildSummaryCard() {
    return Card(
      margin: const EdgeInsets.all(16.0),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
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

            final currencyFormat = NumberFormat.currency(
                locale: 'vi_VN', symbol: '\u20ab');

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Số dư hiện tại:', style: TextStyle(fontSize: 16)),
                Text(
                  currencyFormat.format(balance),
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: balance >= 0 ? Colors.green : Colors.red,
                  ),
                ),
                const Divider(),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Tổng Thu tháng: ${currencyFormat.format(income)}',
                        style: TextStyle(color: Colors.green)),
                    Text('Tổng Chi tháng: ${currencyFormat.format(expense)}',
                        style: TextStyle(color: Colors.red)),
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

            return ListTile(
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
              // Xóa giao dịch khi nhấn giữ
              onLongPress: () async {
                final idRaw = transactionMap[columnTransactionId];
                final int? id = (idRaw is int) ? idRaw : ((idRaw is num) ? idRaw.toInt() : null);
                if (id != null) {
                  await dbHelper.deleteTransaction(id);
                  _refreshData(); // Cập nhật lại UI sau khi xóa
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Đã xóa giao dịch')),
                  );
                }
              },
            );
          },
        );
      },
    );
  }
}
