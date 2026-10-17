# Firestore Security Specification (`security_spec.md`)

## 1. Data Invariants
1. **Authentication Invariant**: No unauthenticated caller (`request.auth == null`) may read or write any document in `/products/{productId}`.
2. **Ownership Isolation**: Every `ProductListing` document in `/products/{productId}` is strictly owned by `userId == request.auth.uid`. Only the owner can read (`get`, `list`), create, update, or delete their own product documents.
3. **Immutable Audit Fields**: `userId` and `createdAt` cannot be modified once created (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
4. **Terminal State Locking**: Once a product listing reaches `status == 'ARCHIVED'`, no further updates are permitted.
5. **Server Timestamps**: `createdAt` and `updatedAt` must be valid Firestore `timestamp` objects `<= request.time`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `auth = null`, `GET /products/prod_1` -> Reject.
2. **Cross-User Read**: `auth.uid = "alice"`, `GET /products/bob_prod` where `userId == "bob"` -> Reject.
3. **Unfiltered List Query**: `auth.uid = "alice"`, `LIST /products` without `.where("userId", "==", "alice")` -> Reject.
4. **Identity Spoofing on Create**: `auth.uid = "alice"`, `CREATE /products/prod_1` with `userId: "bob"` -> Reject.
5. **Shadow Update / Ghost Field**: `auth.uid = "alice"`, `UPDATE /products/prod_1` adding `isVerified: true` -> Reject via `affectedKeys().hasOnly(...)`.
6. **Immutable `userId` Mutation**: `auth.uid = "alice"`, `UPDATE /products/prod_1` changing `userId` to `"bob"` -> Reject.
7. **Immutable `createdAt` Mutation**: `auth.uid = "alice"`, `UPDATE /products/prod_1` changing `createdAt` -> Reject.
8. **Terminal State Mutation**: `auth.uid = "alice"`, `UPDATE /products/prod_1` when `existing().status == "ARCHIVED"` -> Reject.
9. **Value Poisoning (Negative Price)**: `auth.uid = "alice"`, `CREATE /products/prod_1` with `price: -500` -> Reject.
10. **Value Poisoning (Oversized Title)**: `auth.uid = "alice"`, `CREATE /products/prod_1` with 500-char `title` -> Reject.
11. **Invalid Status Enum**: `auth.uid = "alice"`, `CREATE /products/prod_1` with `status: "HACKED"` -> Reject.
12. **Future Timestamp Injection**: `auth.uid = "alice"`, `CREATE /products/prod_1` with `createdAt` in the future (`> request.time`) -> Reject.
