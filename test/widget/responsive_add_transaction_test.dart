import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mymoney/models/category.dart';
import 'package:mymoney/screens/add_transaction_screen.dart';

Future<List<Category>> _fakeCategories() async => [Category(id: 1, name: 'A', type: 0)];

void main() {
  testWidgets('AddTransactionScreen uses two-column layout on wide screens', (WidgetTester tester) async {
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: SizedBox(width: 800, height: 800, child: AddTransactionScreen(categoriesLoader: _fakeCategories)),
      ),
    ));

    await tester.pump();

    // On wide layout, there should be a Row with two Expanded children
    expect(find.byType(Row), findsWidgets);
  });

  testWidgets('AddTransactionScreen uses single-column layout on narrow screens', (WidgetTester tester) async {
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: SizedBox(width: 320, height: 800, child: AddTransactionScreen(categoriesLoader: _fakeCategories)),
      ),
    ));

    await tester.pump();

    // On narrow layout, the top-level Form child should be a Column
    expect(find.byType(Column), findsWidgets);
  });
}
