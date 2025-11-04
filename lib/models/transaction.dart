// models/transaction.dart

// Định nghĩa tên bảng và các cột
final String tableTransaction = 'transactions';
final String columnTransactionId = 'id';
final String columnAmount = 'amount';
final String columnCategoryIdFk = 'categoryId'; // Khóa ngoại
final String columnType = 'type';
final String columnDate = 'date';
final String columnNote = 'note';

class Transaction {
  int? id;
  double amount;
  int categoryId;
  int type; // 0: Expense, 1: Income
  DateTime date;
  String note;

  Transaction({
    this.id,
    required this.amount,
    required this.categoryId,
    required this.type,
    required this.date,
    this.note = '',
  });

  // Chuyển đổi đối tượng Transaction thành Map để lưu vào Database
  Map<String, dynamic> toMap() {
    return {
      columnAmount: amount,
      columnCategoryIdFk: categoryId,
      columnType: type,
      columnDate: date.toIso8601String(),
      // Lưu DateTime dưới dạng chuỗi chuẩn ISO
      columnNote: note,
      if (id != null) columnTransactionId: id,
    };
  }

  // Chuyển đổi từ Map lấy từ Database thành đối tượng Transaction
  Transaction.fromMap(Map<String, dynamic> map)
      : id = map[columnTransactionId],
        amount = map[columnAmount],
        categoryId = map[columnCategoryIdFk],
        type = map[columnType],
        date = DateTime.parse(map[columnDate]), // Phân tích chuỗi thành DateTime
        note = map[columnNote];
}