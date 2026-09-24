CREATE POLICY "users read own bookings by email"
ON public.bookings
FOR SELECT
TO authenticated
USING (lower(email) = lower((auth.jwt() ->> 'email')));