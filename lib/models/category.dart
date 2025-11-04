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

  // Chuyển đổi từ Map lấy từ Database thành đối tượng Category
  Category.fromMap(Map<String, dynamic> map)
      : id = map[columnCategoryId],
        name = map[columnCategoryName],
        type = map[columnCategoryType];
}
