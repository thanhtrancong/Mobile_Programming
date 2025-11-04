import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mymoney/models/category.dart';
import 'package:mymoney/models/transaction.dart' as txn;
import 'package:mymoney/screens/add_transaction_screen.dart';

Future<List<Category>> _fakeCategories() async => [Category(id: 1, name: 'Ăn uống', type: 0)];

void main() {
  testWidgets('AddTransactionScreen adds transaction via onInsert', (WidgetTester tester) async {
    bool inserted = false;

    await tester.pumpWidget(MaterialApp(
      home: Builder(builder: (context) {
        return Scaffold(
          body: Center(
            child: ElevatedButton(
              onPressed: () async {
                await Navigator.push<bool>(
                  context,
                  MaterialPageRoute(builder: (_) => SizedBox(height: 800, child: AddTransactionScreen(
                    categoriesLoader: _fakeCategories,
                    onInsert: (txn.Transaction t) async {
                      inserted = true;
                      return 1;
                    },
                  ))),
                );
              },
              child: const Text('open'),
            ),
          ),
        );
      }),
    ));

    await tester.tap(find.text('open'));
    await tester.pumpAndSettle();

    // Enter amount
    await tester.enterText(find.byType(TextFormField).first, '123.45');
    await tester.pumpAndSettle();

    // Press add button
    await tester.tap(find.text('Thêm'));
    await tester.pumpAndSettle();

    expect(inserted, isTrue);
  });

  testWidgets('AddTransactionScreen updates transaction via onUpdate', (WidgetTester tester) async {
    bool updated = false;
    final existing = txn.Transaction(id: 10, amount: 50.0, categoryId: 1, type: 0, date: DateTime.now(), note: 'note');

    await tester.pumpWidget(MaterialApp(
      home: Builder(builder: (context) {
        return Scaffold(
          body: Center(
            child: ElevatedButton(
              onPressed: () async {
                await Navigator.push<bool>(
                  context,
                  MaterialPageRoute(builder: (_) => SizedBox(height: 800, child: AddTransactionScreen(
                    transaction: existing,
                    categoriesLoader: _fakeCategories,
                    onUpdate: (txn.Transaction t) async {
                      updated = true;
                      return 1;
                    },
                  ))),
                );
              },
              child: const Text('open'),
            ),
          ),
        );
      }),
    ));

    await tester.tap(find.text('open'));
    await tester.pumpAndSettle();

    // Change note
    await tester.enterText(find.byType(TextFormField).at(1), 'updated note');
    await tester.pumpAndSettle();

    // Press save
    await tester.tap(find.text('Lưu'));
    await tester.pumpAndSettle();

    expect(updated, isTrue);
  });
}
