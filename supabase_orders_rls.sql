-- ====================================================================
-- STUDENT MARKET HUB: ORDER PROCESSING STATE MACHINE & RLS POLICIES
-- Platform: Supabase / PostgreSQL
-- Tagline: "Smart Deals, Bigger Dreams"
-- ====================================================================

-- 1. Create Order Status Enum
DO $$ BEGIN
    CREATE TYPE order_status_enum AS ENUM (
        'PENDING',
        'PAID',
        'PROCESSING',
        'DISPATCHED',
        'DELIVERED',
        'CANCELLED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 2. Orders Table Definition
CREATE TABLE IF NOT EXISTS public.orders (
    id TEXT PRIMARY KEY,
    customer_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE RESTRICT,
    customer_name TEXT NOT NULL,
    store_id TEXT NOT NULL REFERENCES public.stores(id) ON DELETE RESTRICT,
    store_name TEXT NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    status order_status_enum NOT NULL DEFAULT 'PENDING',
    paystack_ref TEXT NOT NULL,
    payment_status TEXT NOT NULL DEFAULT 'SUCCESSFUL',
    delivery_campus TEXT NOT NULL,
    delivery_hostel TEXT NOT NULL,
    contact_phone TEXT NOT NULL,
    cancellation_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 3. Enable Row Level Security (RLS) on Orders Table
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

-- ====================================================================
-- RLS POLICIES FOR ORDERS
-- ====================================================================

-- 3.1 SELECT Policy:
DROP POLICY IF EXISTS "orders_select_policy" ON public.orders;
CREATE POLICY "orders_select_policy" ON public.orders
FOR SELECT USING (
    auth.uid()::text = customer_id
    OR
    auth.uid()::text IN (
        SELECT seller_id FROM public.stores WHERE public.stores.id = public.orders.store_id
    )
    OR
    (auth.jwt() ->> 'role') = 'admin'
    OR
    EXISTS (
        SELECT 1 FROM public.profiles
        WHERE public.profiles.id = auth.uid()::text AND public.profiles.role = 'ADMIN'
    )
);

-- 3.2 INSERT Policy:
DROP POLICY IF EXISTS "orders_insert_policy" ON public.orders;
CREATE POLICY "orders_insert_policy" ON public.orders
FOR INSERT WITH CHECK (
    auth.uid()::text = customer_id
    AND status IN ('PENDING', 'PAID')
);

-- 3.3 UPDATE Policy:
DROP POLICY IF EXISTS "orders_update_policy" ON public.orders;
CREATE POLICY "orders_update_policy" ON public.orders
FOR UPDATE USING (
    (
        auth.uid()::text = customer_id
        AND status IN ('PENDING', 'PAID', 'DISPATCHED')
    )
    OR
    (
        auth.uid()::text IN (
            SELECT seller_id FROM public.stores WHERE public.stores.id = public.orders.store_id
        )
        AND status NOT IN ('DELIVERED', 'CANCELLED')
    )
    OR
    (auth.jwt() ->> 'role') = 'admin'
);

-- 3.4 DELETE Policy:
DROP POLICY IF EXISTS "orders_delete_policy" ON public.orders;
CREATE POLICY "orders_delete_policy" ON public.orders
FOR DELETE USING (false);
