import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../db/database_helper.dart';
import '../models/category.dart';
import '../models/transaction.dart' as txn;

class AddTransactionScreen extends StatefulWidget {
  final txn.Transaction? transaction; // nếu không null -> edit mode
  const AddTransactionScreen({super.key, this.transaction});

  @override
  State<AddTransactionScreen> createState() => _AddTransactionScreenState();
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
      _amountController.text = t.amount.toString();
      _noteController.text = t.note;
      _selectedDate = t.date;
      _selectedType = t.type;
      _selectedCategoryId = t.categoryId;
    }
  }

  Future<void> _loadCategories() async {
    final cats = await dbHelper.getCategories();
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
      await dbHelper.insertTransaction(transaction);
    } else {
      await dbHelper.updateTransaction(transaction);
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
        Row(
          children: [
            const Text('Loại:'),
            const SizedBox(width: 8),
            ChoiceChip(
              label: const Text('Chi tiêu'),
              selected: _selectedType == 0,
              onSelected: (_) => setState(() => _selectedType = 0),
            ),
            const SizedBox(width: 8),
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
            Text('Ngày: ${DateFormat('dd/MM/yyyy').format(_selectedDate)}'),
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
