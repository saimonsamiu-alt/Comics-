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

test("Public user: can read published comics", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("comics").doc("comic_1").set({
      title: "Hero Academy",
      synopsis: "Adventure of young heroes",
      authorId: ALICE_UID,
      authorName: "Alice",
      genre: "Action",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("comics").doc("comic_1").get());
});

test("Unauthenticated user: cannot create comic", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("comics").doc("comic_2").set({
      title: "Rogue",
      synopsis: "Test",
      authorId: ALICE_UID,
      authorName: "Alice",
      genre: "Action",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Authenticated user: can create own comic", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("comics").doc("comic_alice").set({
      title: "Alice Adventures",
      synopsis: "Cool comic",
      authorId: ALICE_UID,
      authorName: "Alice",
      genre: "Fantasy",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Authenticated user: cannot delete another user's comic", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("comics").doc("comic_alice").set({
      title: "Alice Adventures",
      synopsis: "Cool comic",
      authorId: ALICE_UID,
      authorName: "Alice",
      genre: "Fantasy",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("comics").doc("comic_alice").delete());
});

test("User bookmarks: isolated to owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("comic_1").set({
      userId: ALICE_UID,
      comicId: "comic_1",
      comicTitle: "Hero Academy",
      lastReadChapter: 1,
      progressPercent: 50,
      updatedAt: new Date()
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("comic_1").get()
  );
});
