CREATE TABLE "payment_transactions" (
	"id" serial PRIMARY KEY,
	"order_id" integer NOT NULL,
	"order_number" text NOT NULL,
	"customer_name" text NOT NULL,
	"paid_at" timestamp with time zone DEFAULT now() NOT NULL,
	"amount" double precision NOT NULL,
	"payment_method" text NOT NULL,
	"reference_no" text DEFAULT '' NOT NULL
);
--> statement-breakpoint
CREATE TABLE "production_batches" (
	"id" serial PRIMARY KEY,
	"batch_code" text NOT NULL,
	"produced_at" timestamp with time zone DEFAULT now() NOT NULL,
	"product_id" integer NOT NULL,
	"product_name" text NOT NULL,
	"quantity_molded" integer NOT NULL,
	"defect_count" integer DEFAULT 0 NOT NULL,
	"cement_bags_used" double precision DEFAULT 0 NOT NULL,
	"sand_used_m3" double precision DEFAULT 0 NOT NULL,
	"aggregate_used_m3" double precision DEFAULT 0 NOT NULL,
	"labor_cost" double precision DEFAULT 0 NOT NULL,
	"supervisor_name" text DEFAULT '' NOT NULL
);
--> statement-breakpoint
CREATE TABLE "products" (
	"id" serial PRIMARY KEY,
	"name" text NOT NULL,
	"size_spec" text NOT NULL,
	"current_stock" integer NOT NULL,
	"unit_cost" double precision NOT NULL,
	"unit_price" double precision NOT NULL,
	"broken_count" integer DEFAULT 0 NOT NULL,
	"min_stock_threshold" integer DEFAULT 500 NOT NULL
);
--> statement-breakpoint
CREATE TABLE "raw_materials" (
	"id" serial PRIMARY KEY,
	"name" text NOT NULL,
	"unit" text NOT NULL,
	"current_stock" double precision NOT NULL,
	"min_alert_threshold" double precision NOT NULL,
	"cost_per_unit" double precision NOT NULL
);
--> statement-breakpoint
CREATE TABLE "sales_orders" (
	"id" serial PRIMARY KEY,
	"order_number" text NOT NULL,
	"customer_name" text NOT NULL,
	"ordered_at" timestamp with time zone DEFAULT now() NOT NULL,
	"product_id" integer NOT NULL,
	"product_name" text NOT NULL,
	"quantity" integer NOT NULL,
	"unit_price" double precision NOT NULL,
	"delivery_fee" double precision DEFAULT 0 NOT NULL,
	"total_amount" double precision NOT NULL,
	"paid_amount" double precision DEFAULT 0 NOT NULL,
	"balance_amount" double precision NOT NULL,
	"payment_status" text NOT NULL,
	"delivery_status" text DEFAULT 'PENDING' NOT NULL
);
--> statement-breakpoint
CREATE TABLE "wage_payments" (
	"id" serial PRIMARY KEY,
	"employee_name" text NOT NULL,
	"paid_at" timestamp with time zone DEFAULT now() NOT NULL,
	"amount" double precision NOT NULL,
	"payment_type" text NOT NULL,
	"payment_method" text NOT NULL
);
--> statement-breakpoint
ALTER TABLE "payment_transactions" ADD CONSTRAINT "payment_transactions_order_id_sales_orders_id_fkey" FOREIGN KEY ("order_id") REFERENCES "sales_orders"("id");--> statement-breakpoint
ALTER TABLE "production_batches" ADD CONSTRAINT "production_batches_product_id_products_id_fkey" FOREIGN KEY ("product_id") REFERENCES "products"("id");--> statement-breakpoint
ALTER TABLE "sales_orders" ADD CONSTRAINT "sales_orders_product_id_products_id_fkey" FOREIGN KEY ("product_id") REFERENCES "products"("id");