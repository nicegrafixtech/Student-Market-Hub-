const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS (Dirty Dozen) ---

test("Unauthenticated user: cannot read products", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").doc("prod_1").get());
});

test("Authenticated user: cannot read another user's product", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("products").doc("bob_prod").set({
      userId: BOB_UID,
      title: "Bob's Calculus Book",
      price: 5000,
      description: "Clean textbook",
      imageUrl: "https://example.com/book.jpg",
      category: "Books & Stationery",
      campus: "University of Lagos (UNILAG)",
      status: "ACTIVE",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("products").doc("bob_prod").get());
});

test("Authenticated user: cannot create product with spoofed userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("products").doc("spoof_prod").set({
      userId: BOB_UID,
      title: "Spoofed Item",
      price: 5000,
      description: "Clean item",
      imageUrl: "https://example.com/item.jpg",
      category: "Electronics",
      campus: "UNILAG",
      status: "ACTIVE",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create and query their own products (matching filter)", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("products").doc("alice_prod").set({
      userId: ALICE_UID,
      title: "Alice's Laptop Stand",
      price: 8500,
      description: "Adjustable aluminium stand",
      imageUrl: "https://example.com/stand.jpg",
      category: "Electronics",
      campus: "University of Lagos (UNILAG)",
      status: "ACTIVE",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("products").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: fails query without userId filter (rules are not filters)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("products").get());
});

test("Authenticated user: rejects shadow update with ghost field", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("products").doc("alice_prod_2").set({
      userId: ALICE_UID,
      title: "Alice's Headphones",
      price: 15000,
      description: "Wireless headphones",
      imageUrl: "https://example.com/hp.jpg",
      category: "Phones & Accessories",
      campus: "UNILAG",
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("products").doc("alice_prod_2").update({
      title: "Updated Headphones",
      isVerified: true,
      updatedAt: new Date(),
    })
  );
});
