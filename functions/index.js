const { onValueCreated } = require("firebase-functions/v2/database");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.database();

const RTP = 92;

exports.placeBet = onValueCreated("/game_bets/{roundId}/{betId}", async (event) => {
  const bet = event.data.val();
  if (!bet) return null;

  const roundId = event.params.roundId;
  const betId = event.params.betId;
  const permanentId = bet.permanentId;
  const betAmount = Number(bet.betAmount);

  if (!permanentId || !betAmount || betAmount <= 0) return null;

  const roundRef = db.ref(`game_rounds/${roundId}`);
  const roundSnap = await roundRef.once("value");
  let round = roundSnap.val() || { totalBet: 0, totalPayout: 0, status: "open" };

  if (round.status !== "open") return null;

  const newTotalBet = (round.totalBet || 0) + betAmount;
  const maxPayoutPool = Math.floor(newTotalBet * RTP / 100);
  const alreadyPaid = round.totalPayout || 0;
  const remainingPool = maxPayoutPool - alreadyPaid;

  let winAmount = 0;
  let isWin = false;

  if (remainingPool > 0) {
    const random = Math.random() * 100;
    if (random < 45) {
      winAmount = Math.min(betAmount * 2, remainingPool);
      isWin = true;
    }
  }

  const userRef = db.ref(`users/${permanentId}/coins`);
  await userRef.transaction((currentCoins) => {
    const coins = currentCoins || 0;
    if (coins < betAmount) return;
    return coins - betAmount + winAmount;
  });

  await roundRef.update({
    totalBet: newTotalBet,
    totalPayout: alreadyPaid + winAmount,
    houseProfit: newTotalBet - (alreadyPaid + winAmount),
    status: "open"
  });

  await db.ref(`coin_ledger/${permanentId}/${betId}`).set({
    roundId: roundId,
    betAmount: betAmount,
    winAmount: winAmount,
    isWin: isWin,
    timestamp: Date.now()
  });

  return null;
});
