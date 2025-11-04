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

  // Chuyển đổi từ Map (lấy từ DB) thành đối tượng Transaction với casting an toàn
  Transaction.fromMap(Map<String, dynamic> map)
      : id = map[columnTransactionId] is int ? map[columnTransactionId] as int : (map[columnTransactionId] is num ? (map[columnTransactionId] as num).toInt() : null),
        amount = (map[columnAmount] is num) ? (map[columnAmount] as num).toDouble() : double.tryParse(map[columnAmount]?.toString() ?? '') ?? 0.0,
        categoryId = (map[columnCategoryIdFk] is int) ? (map[columnCategoryIdFk] as int) : ((map[columnCategoryIdFk] is num) ? (map[columnCategoryIdFk] as num).toInt() : 0),
        type = (map[columnType] is int) ? (map[columnType] as int) : ((map[columnType] is num) ? (map[columnType] as num).toInt() : 0),
        date = map[columnDate] is String ? DateTime.parse(map[columnDate] as String) : DateTime.now(), // fallback to now if parsing fails
        note = (map[columnNote] as String?) ?? '';

}