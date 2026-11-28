const { onValueCreated } = require("firebase-functions/v2/database");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.database();

const MULTS = [45, 25, 15, 10, 5, 5, 5, 5];
const EMOJIS = ["🍗", "🐙", "🦈", "🥩", "🍇", "🥬", "🌽", "🍓"];
const RTP_FIXED = 0.92;

function getTodayKey() {
  const d = new Date(new Date().toLocaleString("en-US", { timeZone: "Asia/Riyadh" }));
  return d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
}

async function resolveUidByProfileId(profileId) {
  if (!profileId) return null;
  const pid = String(profileId).trim();
  const snap = await db.ref("profileIds/" + pid).once("value");
  if (snap.exists()) return String(snap.val());
  const userSnap = await db.ref("users/" + pid).once("value");
  if (userSnap.exists()) return pid;
  return null;
}

async function getUidFromBet(bet) {
  if (bet.permanentId) {
    const uid = await resolveUidByProfileId(bet.permanentId);
    if (uid) return uid;
  }
  if (bet.profileId) {
    const uid = await resolveUidByProfileId(bet.profileId);
    if (uid) return uid;
  }
  return null;
}

// ============ OLD - placeBet - SAME AS BEFORE ============
exports.placeBet = onValueCreated("/game_bets/{roundId}/{betId}", async (event) => {
  const bet = event.data.val();
  if (!bet) return null;
  const roundId = event.params.roundId;
  const betId = event.params.betId;
  const profileId = bet.permanentId || bet.profileId || "";
  const betAmount = Number(bet.betAmount || 0);
  const slot = Number(bet.slot);
  if (!profileId) return null;
  if (betAmount !== 1000 && betAmount !== 5000 && betAmount !== 10000 && betAmount !== 100000) return null;
  if (slot < 0 || slot > 7) return null;
  const roundSnap = await db.ref("game_rounds/" + roundId).once("value");
  const round = roundSnap.val() || { status: "open" };
  if (round.status && round.status !== "open") return null;
  const uid = await getUidFromBet(bet);
  if (!uid) return null;
  const userRef = db.ref("users/" + uid + "/coins");
  let ok = false;
  await userRef.transaction((current) => {
    if (current === null) return current;
    if (current < betAmount) return;
    ok = true;
    return current - betAmount;
  });
  if (!ok) {
    await event.data.ref.remove();
    return null;
  }
  const today = getTodayKey();
  await db.ref("game_rtp/" + today + "/totalBet").transaction((c) => (c || 0) + betAmount);
  await db.ref("game_rtp/" + today + "/totalBetCount").transaction((c) => (c || 0) + 1);
  await db.ref("coin_ledger/" + betId).set({
    uid: uid,
    profileId: String(profileId),
    type: "BET",
    amount: -betAmount,
    roundId: roundId,
    slot: slot,
    at: Date.now()
  });
  await db.ref("companyWallet/totalBet").transaction((c) => (c || 0) + betAmount);
  await db.ref("companyWallet/balance").transaction((c) => (c || 0) + betAmount);
  await db.ref("game_rounds/" + roundId).update({
    totalBet: admin.database.ServerValue.increment(betAmount),
    status: "open",
    updatedAt: Date.now()
  });
  return null;
});

// ============ NEW - 1. AUTO DRAW - 30 SEC POR NIJE NIJE ============
// Proti minute e 2 bar check kore. Open round 30 sec par hoilei trigger dey.
exports.globalDraw = onSchedule("every 1 minutes", async () => {
  for (let attempt = 0; attempt < 2; attempt++) {
    await runAutoDrawIfNeeded();
    if (attempt === 0) await new Promise(r => setTimeout(r, 30000));
  }
});

async function runAutoDrawIfNeeded() {
  const roundsSnap = await db.ref("game_rounds").once("value");
  if (!roundsSnap.exists()) return;
  const now = Date.now();
  const rounds = roundsSnap.val() || {};
  for (const roundId of Object.keys(rounds)) {
    const r = rounds[roundId] || {};
    if (r.status !== "open") continue;
    const created = Number(r.createdAt || r.updatedAt || 0);
    // 30 sec hoy nai - Draw hobe na
    if (created > 0 && now - created < 30000) continue;
    // Ager theke trigger dewa ache kina?
    const trigSnap = await db.ref("game_admin/triggerSettle/" + roundId).once("value");
    if (trigSnap.exists()) continue;
    // Trigger dao - settleRoundAuto nijei Draw dibe
    await db.ref("game_admin/triggerSettle/" + roundId).set({
      auto: true,
      at: now
    });
    await db.ref("game_state/current").update({ bettingOpen: false, round: roundId });
  }
}

// ============ OLD - settleRoundAuto - SAME AS BEFORE ============
exports.settleRoundAuto = onValueCreated("/game_admin/triggerSettle/{roundId}", async (event) => {
  const roundId = event.params.roundId;
  const today = getTodayKey();
  const betsSnap = await db.ref("game_bets/" + roundId).once("value");
  const bets = betsSnap.val() || {};
  const betIds = Object.keys(bets);
  if (betIds.length === 0) return null;
  let betPerSlot = [0, 0, 0, 0, 0, 0, 0, 0];
  let totalBetThisRound = 0;
  for (const betId of betIds) {
    const b = bets[betId];
    const s = Number(b.slot);
    const amt = Number(b.betAmount || 0);
    if (s >= 0 && s <= 7) {
      betPerSlot[s] += amt;
      totalBetThisRound += amt;
    }
  }
  const allowedPayout = Math.floor(totalBetThisRound * RTP_FIXED);
  let candidates = [];
  for (let slot = 0; slot < 8; slot++) {
    candidates.push({ winner: slot, special: "", displayEmoji: EMOJIS[slot], payout: betPerSlot[slot] * MULTS[slot] });
  }
  let saladPayout = 0;
  for (let i = 4; i < 8; i++) saladPayout += betPerSlot[i] * MULTS[i];
  candidates.push({ winner: 4, special: "SALAD", displayEmoji: "🥗", payout: saladPayout });
  let pizzaPayout = 0;
  for (let i = 0; i < 4; i++) pizzaPayout += betPerSlot[i] * MULTS[i];
  candidates.push({ winner: 0, special: "PIZZA", displayEmoji: "🍕", payout: pizzaPayout });
  let safe = [];
  for (let i = 0; i < candidates.length; i++) {
    if (candidates[i].payout <= allowedPayout) safe.push(candidates[i]);
  }
  let chosen = null;
  if (safe.length > 0) {
    let weighted = [];
    for (let i = 0; i < safe.length; i++) {
      let w = safe[i].payout + 2000;
      let count = Math.floor(w / 1000);
      if (count < 1) count = 1;
      if (count > 50) count = 50;
      for (let j = 0; j < count; j++) weighted.push(safe[i]);
    }
    chosen = weighted[Math.floor(Math.random() * weighted.length)];
  } else {
    let sorted = candidates.slice();
    sorted.sort((a, b) => a.payout - b.payout);
    chosen = sorted[0];
  }
  let wins = [];
  let totalPaidThisRound = 0;
  for (const betId of betIds) {
    const b = bets[betId];
    const s = Number(b.slot);
    const amt = Number(b.betAmount || 0);
    let win = 0;
    if (chosen.special === "SALAD" && s >= 4) win = amt * MULTS[s];
    else if (chosen.special === "PIZZA" && s < 4) win = amt * MULTS[s];
    else if (chosen.special === "" && s === chosen.winner) win = amt * MULTS[s];
    if (win > 0) {
      const uid = await getUidFromBet(b);
      if (!uid) continue;
      const profileId = b.permanentId || b.profileId || "";
      totalPaidThisRound += win;
      wins.push({ uid: uid, userName: b.userName || "Player", win: win });
      await db.ref("users/" + uid + "/coins").transaction((c) => (c || 0) + win);
      await db.ref("coin_ledger/" + roundId + "_" + uid + "_" + betId).set({
        uid: uid,
        profileId: String(profileId),
        type: "WIN",
        amount: win,
        roundId: roundId,
        at: Date.now()
      });
      await db.ref("companyWallet/totalPaid").transaction((c) => (c || 0) + win);
      await db.ref("companyWallet/balance").transaction((c) => (c || 0) - win);
    }
  }
  await db.ref("game_rtp/" + today + "/totalPaid").transaction((c) => (c || 0) + totalPaidThisRound);
  await db.ref("game_rtp/" + today).update({
    lastRound: roundId,
    lastWinner: chosen.winner,
    lastSpecial: chosen.special,
    lastPayout: totalPaidThisRound,
    lastTotalBet: totalBetThisRound,
    lastProfit: totalBetThisRound - totalPaidThisRound,
    lastBetPerSlot: betPerSlot,
    updatedAt: Date.now()
  });
  await db.ref("game_rounds/" + roundId).update({
    totalBet: totalBetThisRound,
    totalPayout: totalPaidThisRound,
    houseProfit: totalBetThisRound - totalPaidThisRound,
    winner: chosen.winner,
    special: chosen.special,
    status: "settled",
    settledAt: Date.now()
  });
  wins.sort((a, b) => b.win - a.win);
  let top3 = [];
  for (let i = 0; i < wins.length && i < 3; i++) {
    top3.push({ userName: wins[i].userName, avatar: "🦁", win: wins[i].win });
  }
  await db.ref("game_results/" + roundId).set({
    winner: chosen.winner,
    special: chosen.special,
    displayEmoji: chosen.displayEmoji,
    payout: totalPaidThisRound,
    totalBet: totalBetThisRound,
    betPerSlot: betPerSlot,
    settledAt: Date.now()
  });
  await db.ref("game_top3/" + roundId).set(top3);
  await db.ref("game_state/current").update({ bettingOpen: true, lastRound: roundId });
  return null;
});

// ============ OLD - dailySettlement - SAME AS BEFORE ============
exports.dailySettlement = onValueCreated("/game_admin/dailySettlement/{date}", async (event) => {
  const date = event.params.date;
  const rtpSnap = await db.ref("game_rtp/" + date).once("value");
  const rtp = rtpSnap.val() || {};
  const totalBet = Number(rtp.totalBet || 0);
  const totalPaid = Number(rtp.totalPaid || 0);
  await db.ref("game_settlement/" + date).set({
    date: date,
    totalBet: totalBet,
    totalPaid: totalPaid,
    profit: totalBet - totalPaid,
    rtp: totalBet > 0 ? totalPaid / totalBet : 0,
    targetRTP: RTP_FIXED,
    settledAt: Date.now()
  });
  return null;
});

// ============ OLD - targetByProfileId - SAME AS BEFORE ============
exports.targetByProfileId = onValueCreated("/game_admin/target/{pushId}", async (event) => {
  const req = event.data.val();
  if (!req) return null;
  const profileId = req.profileId || req.permanentId || req.targetProfileId || "";
  const action = req.action || "";
  if (!profileId || !action) return null;
  const uid = await resolveUidByProfileId(profileId);
  if (!uid) return null;
  const userRef = db.ref("users/" + uid);
  if (action === "ADD_COINS") {
    const coins = Number(req.coins || req.amount || 0);
    if (coins <= 0) return null;
    await userRef.child("coins").transaction((c) => (c || 0) + coins);
  } else if (action === "REMOVE_COINS") {
    const coins = Number(req.coins || req.amount || 0);
    if (coins <= 0) return null;
    await userRef.child("coins").transaction((c) => Math.max(0, (c || 0) - coins));
  } else if (action === "GIVE_VIP") {
    const vipDays = Number(req.vipDays || 0);
    if (vipDays <= 0) return null;
    await userRef.update({ vip: true, vipUntil: Date.now() + vipDays * 24 * 60 * 60 * 1000 });
  } else if (action === "GIVE_FRAME") {
    const frameId = req.frameId || req.itemId || "";
    if (!frameId) return null;
    await userRef.child("frames/" + frameId).set(true);
  } else if (action === "GIVE_BADGE") {
    const badgeId = req.badgeId || req.itemId || "";
    if (!badgeId) return null;
    await userRef.child("badges/" + badgeId).set(true);
  } else if (action === "BLOCK_USER") {
    await userRef.update({ blocked: true });
  } else if (action === "UNBLOCK_USER") {
    await userRef.update({ blocked: false });
  } else {
    return null;
  }
  await db.ref("coin_ledger/" + event.params.pushId).set({
    uid: uid,
    profileId: String(profileId),
    type: action,
    amount: Number(req.coins || req.amount || 0),
    at: Date.now()
  });
  return null;
});

// ============ NEW - 2. GIFT - SERVER THEKE ============
exports.onGiftCreated = onValueCreated("/coin_transactions/{giftId}", async (event) => {
  const g = event.data.val();
  if (!g || g.type !== "GIFT") return null;
  const fromUid = g.fromUid, toUid = g.toUid;
  const amount = Number(g.amount || 0);
  if (!fromUid || !toUid || amount <= 0) return null;
  await db.ref("users/" + fromUid + "/coins").transaction((cur) => (cur || 0) - amount);
  await db.ref("users/" + toUid + "/coins").transaction((cur) => (cur || 0) + amount);
  await db.ref("coin_ledger").push().set({
    uid: fromUid, type: "GIFT_SENT", toUid: toUid, amount: -amount, at: Date.now()
  });
  await db.ref("coin_ledger").push().set({
    uid: toUid, type: "GIFT_RECEIVED", fromUid: fromUid, amount: amount, at: Date.now()
  });
  return null;
});
