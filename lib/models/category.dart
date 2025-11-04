// models/category.dart

// Định nghĩa tên bảng và các cột
final String tableCategory = 'categories';
final String columnCategoryId = 'id';
final String columnCategoryName = 'name';
final String columnCategoryType = 'type';
// 0: Chi tiêu (Expense), 1: Thu nhập (Income)

class Category {
  int? id;
  String name;
  int type;

  Category({this.id, required this.name, required this.type});

  // Chuyển đổi đối tượng Category thành Map để lưu vào Database
  Map<String, dynamic> toMap() {
    return {
      columnCategoryName: name,
      columnCategoryType: type,
      if (id != null) columnCategoryId: id, // Chỉ thêm id nếu nó tồn tại
    };
  }

  // Chuyển đổi từ Map lấy từ Database thành đối tượng Category (cast an toàn)
  Category.fromMap(Map<String, dynamic> map)
      : id = map[columnCategoryId] is int ? map[columnCategoryId] as int : (map[columnCategoryId] is num ? (map[columnCategoryId] as num).toInt() : null),
        name = (map[columnCategoryName] as String?) ?? '',
        type = (map[columnCategoryType] is int) ? (map[columnCategoryType] as int) : ((map[columnCategoryType] is num) ? (map[columnCategoryType] as num).toInt() : 0);

}
