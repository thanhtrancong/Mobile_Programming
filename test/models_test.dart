import 'package:flutter_test/flutter_test.dart';
import 'package:mymoney/models/category.dart';
import 'package:mymoney/models/transaction.dart';

void main() {
  test('Category.fromMap handles types correctly', () {
    final map = { 'id': 5, 'name': 'Test', 'type': 0 };
    final c = Category.fromMap(map);
    expect(c.id, 5);
    expect(c.name, 'Test');
    expect(c.type, 0);

    final map2 = { 'id': 7.0, 'name': null, 'type': 1.0 };
    final c2 = Category.fromMap(map2);
    expect(c2.id, 7);
    expect(c2.name, '');
    expect(c2.type, 1);
  });

  test('Transaction.fromMap handles numeric and null correctly', () {
    final map = {
      'id': 3,
      'amount': 123.45,
      'categoryId': 2,
      'type': 0,
      'date': '2023-01-02T00:00:00.000',
      'note': 'hello'
    };
    final t = Transaction.fromMap(map);
    expect(t.id, 3);
    expect(t.amount, 123.45);
    expect(t.categoryId, 2);
    expect(t.type, 0);
    expect(t.note, 'hello');

    final map2 = {
      'id': null,
      'amount': '200',
      'categoryId': 1.0,
      'type': 1.0,
      'date': 'invalid',
      'note': null
    };
    final t2 = Transaction.fromMap(map2);
    expect(t2.id, null);
    expect(t2.amount, 200.0);
    expect(t2.categoryId, 1);
    expect(t2.type, 1);
    expect(t2.note, '');
  });
}

