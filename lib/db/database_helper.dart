// db/database_helper.dart
import 'package:sqflite/sqflite.dart' hide Transaction;
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart';
import 'dart:io';

import '../models/category.dart';
import '../models/transaction.dart';

class DatabaseHelper {
  // Singleton Pattern: Đảm bảo chỉ có 1 instance của lớp này
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  factory DatabaseHelper() => _instance;
  DatabaseHelper._internal();

  static Database? _database;

  // Getter cho Database, nếu chưa khởi tạo thì gọi _initDatabase
  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  // Khởi tạo Database
  Future<Database> _initDatabase() async {
    Directory documentsDirectory = await getApplicationDocumentsDirectory();
    String path = join(documentsDirectory.path, 'mymoney_database.db');

    // Mở Database với version 1. Nếu DB chưa tồn tại, onCreate sẽ được gọi
    return await openDatabase(path, version: 1, onCreate: _onCreate,
        onConfigure: _onConfigure);
  }

  // Bật chế độ khóa ngoại (FOREIGN KEY)
  Future _onConfigure(Database db) async {
    await db.execute('PRAGMA foreign_keys = ON');
  }

  // Tạo bảng khi Database được khởi tạo lần đầu
  void _onCreate(Database db, int version) async {
    // 1. Tạo bảng CATEGORY
    await db.execute('''
      CREATE TABLE $tableCategory (
        $columnCategoryId INTEGER PRIMARY KEY,
        $columnCategoryName TEXT NOT NULL,
        $columnCategoryType INTEGER NOT NULL
      )
    ''');

    // 2. Tạo bảng TRANSACTION với Khóa ngoại (FOREIGN KEY)
    await db.execute('''
      CREATE TABLE $tableTransaction (
        $columnTransactionId INTEGER PRIMARY KEY,
        $columnAmount REAL NOT NULL,
        $columnCategoryIdFk INTEGER NOT NULL,
        $columnType INTEGER NOT NULL,
        $columnDate TEXT NOT NULL,
        $columnNote TEXT,
        FOREIGN KEY ($columnCategoryIdFk) REFERENCES $tableCategory 
        ($columnCategoryId) 
          ON DELETE CASCADE 
      )
    ''');

    // *** THÊM DỮ LIỆU BAN ĐẦU (SEEDING) ***
    // Thêm các danh mục mặc định để ứng dụng có thể dùng ngay
    await db.insert(tableCategory, Category(name: 'Ăn uống', type: 0).toMap());
    await db.insert(tableCategory, Category(name: 'Di chuyển', type: 0).toMap());
    await db.insert(tableCategory, Category(name: 'Lương', type: 1).toMap());
    await db.insert(tableCategory, Category(name: 'Tiền thưởng', type: 1).toMap());
  }

  // =================================================================
  // --- CHỨC NĂNG CHO BẢNG CATEGORY ---
  // =================================================================

  // CREATE Category
  Future<int> insertCategory(Category category) async {
    Database db = await database;
    return await db.insert(tableCategory, category.toMap());
  }

  // READ Categories
  Future<List<Category>> getCategories() async {
    Database db = await database;
    List<Map<String, dynamic>> maps = await db.query(tableCategory);
    return List.generate(maps.length, (i) {
      return Category.fromMap(maps[i]);
    });
  }

  // =================================================================
  // --- CHỨC NĂNG CHO BẢNG TRANSACTION (CRUD + Report) ---
  // =================================================================

  // CREATE Transaction
  Future<int> insertTransaction(Transaction transaction) async {
    Database db = await database;
    return await db.insert(tableTransaction, transaction.toMap());
  }

  // READ Transactions (Lấy giao dịch kèm tên danh mục)
  Future<List<Map<String, dynamic>>> getTransactionsWithCategory() async {
    Database db = await database;
    // Sử dụng JOIN để lấy tên Danh mục (c.name) cùng với dữ liệu giao dịch (t.*)
    return await db.rawQuery('''
      SELECT t.*, c.name as categoryName 
      FROM $tableTransaction t
      INNER JOIN $tableCategory c ON t.$columnCategoryIdFk = c.$columnCategoryId
      ORDER BY t.$columnDate DESC
    ''');
  }

  // DELETE Transaction
  Future<int> deleteTransaction(int id) async {
    Database db = await database;
    return await db.delete(
        tableTransaction,
        where: '$columnTransactionId = ?',
        whereArgs: [id]
    );
  }

  // REPORT: Tính tổng thu/chi trong tháng hiện tại
  Future<double> calculateTotal(int type) async {
    Database db = await database;
    var now = DateTime.now();
    // Tạo chuỗi ngày đầu và ngày cuối tháng để query theo
    var startOfMonth = DateTime(now.year, now.month, 1).toIso8601String();
    var endOfMonth = DateTime(now.year, now.month + 1, 0).toIso8601String();

    List<Map> result = await db.rawQuery('''
      SELECT SUM($columnAmount) as total 
      FROM $tableTransaction 
      WHERE $columnType = ? 
      AND $columnDate >= ? 
      AND $columnDate <= ?
    ''', [type, startOfMonth, endOfMonth]);

    // Lấy giá trị tổng (có thể là null nếu không có giao dịch)
    double total = result.first['total'] ?? 0.0;
    return total;
  }
}