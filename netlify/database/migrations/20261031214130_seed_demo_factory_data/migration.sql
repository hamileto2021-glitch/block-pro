-- Demo data mirroring the Android app's initial seed (prices in ETB)
INSERT INTO "raw_materials" ("name", "unit", "current_stock", "min_alert_threshold", "cost_per_unit") VALUES
  ('Dangote / Derba Cement 42.5R', 'Bags', 280, 60, 1650),
  ('Awash River Sand', 'm³', 65, 20, 1100),
  ('Basalt Crushed Aggregate 01/02', 'm³', 75, 25, 1250),
  ('Water Tanker Delivery', 'Liters', 8000, 2000, 0.35),
  ('Concrete Accelerator / Hardener', 'Liters', 120, 30, 280),
  ('Heavy Duty Pallets', 'Units', 450, 120, 350);
--> statement-breakpoint
INSERT INTO "products" ("name", "size_spec", "current_stock", "unit_cost", "unit_price", "broken_count", "min_stock_threshold") VALUES
  ('Blocket 10', '40 x 20 x 10 cm', 3800, 26, 38, 24, 800),
  ('Blocket 15', '40 x 20 x 15 cm', 5600, 38, 54, 35, 1200),
  ('Blocket 20', '40 x 20 x 20 cm', 3200, 52, 74, 28, 900);
--> statement-breakpoint
INSERT INTO "production_batches" ("batch_code", "produced_at", "product_id", "product_name", "quantity_molded", "defect_count", "cement_bags_used", "sand_used_m3", "aggregate_used_m3", "labor_cost", "supervisor_name") VALUES
  ('PRD-ETH-098', now() - interval '2 days', 2, 'Blocket 15', 1600, 14, 27, 5.8, 7.2, 3200, 'Selamawit Tadesse'),
  ('PRD-ETH-099', now() - interval '1 day', 3, 'Blocket 20', 1200, 12, 25, 5.5, 7.0, 2400, 'Selamawit Tadesse'),
  ('PRD-ETH-100', now() - interval '3 hours', 1, 'Blocket 10', 2400, 18, 26, 5.4, 6.8, 4800, 'Selamawit Tadesse'),
  ('PRD-ETH-101', now(), 2, 'Blocket 15', 1800, 15, 28, 6.0, 7.5, 3600, 'Selamawit Tadesse');
--> statement-breakpoint
INSERT INTO "wage_payments" ("employee_name", "paid_at", "amount", "payment_type", "payment_method") VALUES
  ('Getachew Bekele', now() - interval '2 hours', 840, 'PIECE_PAYMENT', 'TELEBIRR'),
  ('Biruk Yohannes', now() - interval '2 hours', 840, 'PIECE_PAYMENT', 'TELEBIRR'),
  ('Dawit Kebede', now() - interval '1 day', 450, 'DAILY_WAGE', 'CBE_BIRR'),
  ('Ermias Tefera', now() - interval '1 day', 450, 'DAILY_WAGE', 'CASH');
--> statement-breakpoint
INSERT INTO "sales_orders" ("order_number", "customer_name", "ordered_at", "product_id", "product_name", "quantity", "unit_price", "delivery_fee", "total_amount", "paid_amount", "balance_amount", "payment_status", "delivery_status") VALUES
  ('ORD-AA-398', 'Ovid Construction', now() - interval '2 days', 2, 'Blocket 15', 3000, 52, 4500, 160500, 100000, 60500, 'PARTIAL', 'DELIVERED'),
  ('ORD-AA-399', 'Afro-Tsion Construction', now() - interval '1 day', 3, 'Blocket 20', 1200, 74, 2800, 91600, 91600, 0, 'PAID', 'DELIVERED'),
  ('ORD-AA-400', 'Flintstone Engineering', now() - interval '4 hours', 1, 'Blocket 10', 2500, 38, 3000, 98000, 98000, 0, 'PAID', 'DELIVERED'),
  ('ORD-AA-401', 'Sunshine Construction PLC', now() - interval '1 hour', 2, 'Blocket 15', 1500, 54, 2500, 83500, 50000, 33500, 'PARTIAL', 'DISPATCHED');
--> statement-breakpoint
INSERT INTO "payment_transactions" ("order_id", "order_number", "customer_name", "paid_at", "amount", "payment_method", "reference_no") VALUES
  (1, 'ORD-AA-398', 'Ovid Construction', now() - interval '2 days', 100000, 'BANK_TRANSFER', 'ABY-FT-77291'),
  (2, 'ORD-AA-399', 'Afro-Tsion Construction', now() - interval '1 day', 91600, 'TELEBIRR', 'TB-88492019'),
  (3, 'ORD-AA-400', 'Flintstone Engineering', now() - interval '4 hours', 98000, 'BANK_TRANSFER', 'AWASH-WIRE-4412'),
  (4, 'ORD-AA-401', 'Sunshine Construction PLC', now() - interval '1 hour', 50000, 'CBE_BIRR', 'CBE-TX-998412');
