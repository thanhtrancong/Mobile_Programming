import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import '../db/database_helper.dart';
import '../models/category.dart';
import '../models/transaction.dart' as txn;

typedef CategoriesLoader = Future<List<Category>> Function();
typedef TransactionSaver = Future<int> Function(txn.Transaction transaction);

class AddTransactionScreen extends StatefulWidget {
  final txn.Transaction? transaction; // nếu không null -> edit mode
  final CategoriesLoader? categoriesLoader;
  final TransactionSaver? onInsert;
  final TransactionSaver? onUpdate;

  const AddTransactionScreen({super.key, this.transaction, this.categoriesLoader, this.onInsert, this.onUpdate});

  @override
  State<AddTransactionScreen> createState() => _AddTransactionScreenState();
}

// Custom input formatter: allow digits and one decimal separator (dot or comma), up to 2 decimals
class AmountInputFormatter extends TextInputFormatter {
  final _normalized = RegExp(r'[^0-9\.,]');
  final _valid = RegExp(r'^\d*(?:[\.,]\d{0,2})?$');

  @override
  TextEditingValue formatEditUpdate(TextEditingValue oldValue, TextEditingValue newValue) {
    String text = newValue.text;
    // Remove any characters except digits and separators
    text = text.replaceAll(_normalized, '');
    if (text.isEmpty) return newValue.copyWith(text: '');

    // Normalize multiple separators to single
    int dotCount = '.'.allMatches(text).length;
    int commaCount = ','.allMatches(text).length;
    if (dotCount + commaCount > 1) {
      return oldValue;
    }

    // Validate pattern (allow either comma or dot)
    if (!_valid.hasMatch(text)) {
      return oldValue;
    }

    return newValue.copyWith(text: text, selection: updateCursor(text, newValue.selection));
  }

  TextSelection updateCursor(String text, TextSelection selection) {
    final int offset = selection.baseOffset.clamp(0, text.length);
    return TextSelection.collapsed(offset: offset);
  }
}

class _AddTransactionScreenState extends State<AddTransactionScreen> {
  final _formKey = GlobalKey<FormState>();
  final dbHelper = DatabaseHelper();

  final TextEditingController _amountController = TextEditingController();
  final TextEditingController _noteController = TextEditingController();
  DateTime _selectedDate = DateTime.now();
  int _selectedType = 0; // 0 expense, 1 income
  int? _selectedCategoryId;

  List<Category> _categories = [];
  bool _loadingCategories = true;

  @override
  void initState() {
    super.initState();
    _loadCategories();

    if (widget.transaction != null) {
      final t = widget.transaction!;
      // format amount for display (use dot as decimal separator)
      _amountController.text = t.amount.toString();
      _noteController.text = t.note;
      _selectedDate = t.date;
      _selectedType = t.type;
      _selectedCategoryId = t.categoryId;
    }
  }

  Future<void> _loadCategories() async {
    final cats = widget.categoriesLoader != null ? await widget.categoriesLoader!() : await dbHelper.getCategories();
    if (!mounted) return;
    setState(() {
      _categories = cats;
      _loadingCategories = false;
      // If no selected category set and categories exist, pick first
      if (_selectedCategoryId == null && _categories.isNotEmpty) {
        _selectedCategoryId = _categories.first.id;
      }
    });
  }

  @override
  void dispose() {
    _amountController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  Future<void> _pickDate() async {
    final dt = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );
    if (dt != null && mounted) setState(() => _selectedDate = dt);
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    final amount = double.tryParse(_amountController.text.replaceAll(',', '').trim()) ?? 0.0;
    if (amount <= 0) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Số tiền phải lớn hơn 0')));
      return;
    }
    if (_selectedCategoryId == null) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Vui lòng chọn danh mục')));
      return;
    }

    final transaction = txn.Transaction(
      id: widget.transaction?.id,
      amount: amount,
      categoryId: _selectedCategoryId!,
      type: _selectedType,
      date: _selectedDate,
      note: _noteController.text.trim(),
    );

    if (widget.transaction == null) {
      if (widget.onInsert != null) {
        await widget.onInsert!(transaction);
      } else {
        await dbHelper.insertTransaction(transaction);
      }
    } else {
      if (widget.onUpdate != null) {
        await widget.onUpdate!(transaction);
      } else {
        await dbHelper.updateTransaction(transaction);
      }
    }

    if (!mounted) return; // ensure context still valid
    Navigator.of(context).pop(true); // thông báo đã thay đổi
  }

  Widget _formFields(double width) {
    // width parameter helps adjust spacing/field widths if needed
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextFormField(
          controller: _amountController,
          keyboardType: TextInputType.numberWithOptions(decimal: true),
          decoration: const InputDecoration(labelText: 'Số tiền', prefixText: ''),
          inputFormatters: [AmountInputFormatter()],
          validator: (v) => (v == null || v.trim().isEmpty) ? 'Nhập số tiền' : null,
        ),
        const SizedBox(height: 12),
        DropdownButtonFormField<int>(
          initialValue: _selectedCategoryId,
          decoration: const InputDecoration(labelText: 'Danh mục'),
          items: _categories.map((c) => DropdownMenuItem<int>(value: c.id, child: Text(c.name))).toList(),
          onChanged: (v) => setState(() => _selectedCategoryId = v),
        ),
        const SizedBox(height: 12),
        TextFormField(
          controller: _noteController,
          decoration: const InputDecoration(labelText: 'Ghi chú (tùy chọn)'),
        ),
      ],
    );
  }

  Widget _formControls() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Wrap(
          spacing: 8,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            const Text('Loại:'),
            ChoiceChip(
              label: const Text('Chi tiêu'),
              selected: _selectedType == 0,
              onSelected: (_) => setState(() => _selectedType = 0),
            ),
            ChoiceChip(
              label: const Text('Thu nhập'),
              selected: _selectedType == 1,
              onSelected: (_) => setState(() => _selectedType = 1),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Flexible(child: Text('Ngày: ${DateFormat('dd/MM/yyyy').format(_selectedDate)}')),
            const SizedBox(width: 16),
            ElevatedButton(onPressed: _pickDate, child: const Text('Chọn ngày')),
          ],
        ),
        const SizedBox(height: 16),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton(onPressed: _save, child: Text(widget.transaction == null ? 'Thêm' : 'Lưu')),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.transaction == null ? 'Thêm giao dịch' : 'Chỉnh sửa giao dịch')),
      body: LayoutBuilder(
        builder: (context, constraints) {
          return Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 900),
              child: _loadingCategories
                  ? const Center(child: CircularProgressIndicator())
                  : SingleChildScrollView(
                      padding: const EdgeInsets.all(16.0),
                      child: Form(
                        key: _formKey,
                        child: constraints.maxWidth >= 700
                            ? Row(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  // left column: fields
                                  Expanded(flex: 2, child: _formFields(constraints.maxWidth * 0.6)),
                                  const SizedBox(width: 16),
                                  // right column: controls
                                  Expanded(flex: 1, child: _formControls()),
                                ],
                              )
                            : Column(
                                crossAxisAlignment: CrossAxisAlignment.stretch,
                                children: [
                                  _formFields(constraints.maxWidth),
                                  const SizedBox(height: 12),
                                  _formControls(),
                                ],
                              ),
                      ),
                    ),
            ),
          );
        },
      ),
    );
  }
}
