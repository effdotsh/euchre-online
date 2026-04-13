const root = document.getElementById("game-root");
const statusCopy = document.getElementById("status-copy");

const state = {
    snapshot: null,
    submitting: false,
    fetchInFlight: false,
    clickTargets: [],
    status: "Connecting to game...",
    hoveredTarget: null
};

async function loadSnapshot() {
    const response = await fetch("/api/game", { cache: "no-store" });
    if (!response.ok) {
        throw new Error(`Snapshot request failed: ${response.status}`);
    }
    return response.json();
}

async function submitAction(value) {
    state.submitting = true;
    try {
        const response = await fetch("/api/action", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body: new URLSearchParams({ value }).toString()
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }
    } finally {
        state.submitting = false;
    }
}

async function refresh() {
    if (state.fetchInFlight) {
        return;
    }

    state.fetchInFlight = true;
    try {
        state.snapshot = await loadSnapshot();
        state.status = buildStatus(state.snapshot);
        statusCopy.textContent = state.status;
    } catch (error) {
        state.status = error.message;
        statusCopy.textContent = error.message;
    } finally {
        state.fetchInFlight = false;
    }
}

function buildStatus(snapshot) {
    if (!snapshot) {
        return "Connecting to game...";
    }
    if (snapshot.isOver) {
        return `Game over. Blue ${snapshot.bluePoints}, Red ${snapshot.redPoints}.`;
    }

    const remotePlayer = snapshot.players.find(player => player.isRemote);
    const pending = remotePlayer?.pendingAction;
    if (!pending) {
        return "Waiting for the next decision...";
    }
    if (pending.type === "play_card") {
        return pending.ledSuit
            ? `Your turn. Follow ${formatSuit(pending.ledSuit)} if you can.`
            : "Your turn. Lead a card.";
    }
    if (pending.type === "order_up") {
        return `Order up ${pending.upCard.label} by clicking a discard, or pass.`;
    }
    return "Choose trump or pass.";
}

function isRed(suit) {
    return suit === "HEARTS" || suit === "DIAMONDS";
}

function formatSuit(suit) {
    if (suit === "HEARTS") return "Hearts";
    if (suit === "DIAMONDS") return "Diamonds";
    if (suit === "CLUBS") return "Clubs";
    return "Spades";
}

function seatBadge(player, hand) {
    if (player.name === hand.dealerName && player.name === hand.callerName) {
        return "Dealer, Caller";
    }
    if (player.name === hand.dealerName) {
        return "Dealer";
    }
    if (player.name === hand.callerName) {
        return "Caller";
    }
    return `${player.handSize} cards`;
}

function containsPoint(target, x, y) {
    return x >= target.x && x <= target.x + target.w && y >= target.y && y <= target.y + target.h;
}

function addTarget(target) {
    state.clickTargets.push(target);
}

new p5(p => {
    p.setup = () => {
        const canvas = p.createCanvas(root.clientWidth, canvasHeight());
        canvas.parent(root);
        p.textFont("Georgia");
        p.textAlign(p.LEFT, p.BASELINE);
        p.noStroke();

        refresh();
        window.setInterval(refresh, 350);
    };

    p.windowResized = () => {
        p.resizeCanvas(root.clientWidth, canvasHeight());
    };

    p.mouseMoved = () => {
        updateHover(p.mouseX, p.mouseY);
    };

    p.mousePressed = async () => {
        await handlePress(p.mouseX, p.mouseY);
    };

    p.touchStarted = async () => {
        await handlePress(p.mouseX, p.mouseY);
        return false;
    };

    p.draw = () => {
        state.clickTargets = [];
        state.hoveredTarget = findHoveredTarget(p.mouseX, p.mouseY);
        root.style.cursor = state.hoveredTarget ? "pointer" : "default";

        drawPage(p);
        if (!state.snapshot || !state.snapshot.currentHand) {
            drawLoadingState(p);
            return;
        }

        drawGame(p, state.snapshot);
    };
});

function canvasHeight() {
    return Math.max(root.clientWidth < 820 ? 980 : 820, window.innerHeight - 40);
}

async function handlePress(x, y) {
    const target = [...state.clickTargets].reverse().find(candidate => containsPoint(candidate, x, y));
    if (!target || state.submitting) {
        return;
    }

    try {
        await submitAction(target.value);
        statusCopy.textContent = "Submitting action...";
        await refresh();
    } catch (error) {
        statusCopy.textContent = error.message;
    }
}

function updateHover(x, y) {
    state.hoveredTarget = findHoveredTarget(x, y);
    root.style.cursor = state.hoveredTarget ? "pointer" : "default";
}

function findHoveredTarget(x, y) {
    return [...state.clickTargets].reverse().find(target => containsPoint(target, x, y)) ?? null;
}

function drawPage(p) {
    p.background(240, 231, 214);
    p.noStroke();
    p.fill(255, 251, 245, 226);
    p.rect(12, 12, p.width - 24, p.height - 24, 28);
}

function drawLoadingState(p) {
    p.fill(36, 29, 23);
    p.textAlign(p.CENTER, p.CENTER);
    p.textSize(28);
    p.text("Loading Euchre table...", p.width / 2, p.height / 2 - 20);
    p.textSize(18);
    p.fill(94, 80, 67);
    p.text(state.status, p.width / 2, p.height / 2 + 24);
    p.textAlign(p.LEFT, p.BASELINE);
}

function drawGame(p, snapshot) {
    const hand = snapshot.currentHand;
    const remotePlayer = snapshot.players.find(player => player.isRemote);
    const pending = remotePlayer?.pendingAction ?? null;
    const layout = getLayout(p);

    drawTopPanel(p, snapshot, hand, layout);
    drawTable(p, layout);

    drawSeat(p, hand.players[0], hand, pending, layout.north, "north");
    drawSeat(p, hand.players[1], hand, pending, layout.east, "east");
    drawSeat(p, hand.players[2], hand, pending, layout.south, "south");
    drawSeat(p, hand.players[3], hand, pending, layout.west, "west");

    drawUpCard(p, hand.upCard, layout.upCardBox);
    drawTrick(p, hand.currentTrick, layout, hand.players);
    drawActionStrip(p, snapshot, remotePlayer, layout);
}

function getLayout(p) {
    const mobile = p.width < 820;
    const pad = mobile ? 18 : 28;
    const contentX = pad;
    const contentY = pad;
    const contentW = p.width - pad * 2;
    const topPanelH = mobile ? 164 : 118;
    const actionH = mobile ? 136 : 108;
    const tableY = contentY + topPanelH + 14;
    const tableH = p.height - tableY - actionH - pad;
    const tableX = contentX;
    const tableW = contentW;

    if (mobile) {
        const northW = Math.max(160, tableW - 132);
        const sideW = (tableW - 42) / 2;
        const sideY = tableY + tableH * 0.49 - 52;
        return {
            mobile,
            pad,
            contentX,
            contentY,
            contentW,
            topPanelH,
            actionH,
            tableX,
            tableY,
            tableW,
            tableH,
            north: { x: tableX + 14, y: tableY + 14, w: northW, h: 100 },
            south: { x: tableX + 14, y: tableY + tableH - 148, w: tableW - 28, h: 130 },
            west: { x: tableX + 14, y: sideY, w: sideW, h: 96 },
            east: { x: tableX + tableW - 14 - sideW, y: sideY, w: sideW, h: 96 },
            trickCenterX: tableX + tableW * 0.5,
            trickCenterY: tableY + tableH * 0.5,
            upCardBox: { x: tableX + tableW - 102, y: tableY + 14, w: 88, h: 112 },
            actionBox: { x: contentX, y: tableY + tableH + 14, w: contentW, h: actionH }
        };
    }

    return {
        mobile,
        pad,
        contentX,
        contentY,
        contentW,
        topPanelH,
        actionH,
        tableX,
        tableY,
        tableW,
        tableH,
        north: { x: tableX + tableW * 0.5 - 180, y: tableY + 24, w: 360, h: 118 },
        south: { x: tableX + tableW * 0.5 - 235, y: tableY + tableH - 156, w: 470, h: 132 },
        west: { x: tableX + 22, y: tableY + tableH * 0.5 - 90, w: 214, h: 132 },
        east: { x: tableX + tableW - 236, y: tableY + tableH * 0.5 - 90, w: 214, h: 132 },
        trickCenterX: tableX + tableW * 0.5,
        trickCenterY: tableY + tableH * 0.5,
        upCardBox: { x: tableX + tableW - 140, y: tableY + 26, w: 98, h: 130 },
        actionBox: { x: contentX, y: tableY + tableH + 18, w: contentW, h: actionH }
    };
}

function drawTopPanel(p, snapshot, hand, layout) {
    p.fill(255, 250, 244, 230);
    p.stroke(121, 97, 70, 22);
    p.strokeWeight(1);
    p.rect(layout.contentX, layout.contentY, layout.contentW, layout.topPanelH, 24);
    p.noStroke();

    if (layout.mobile) {
        const columnW = (layout.contentW - 56) / 3;
        drawMetric(p, layout.contentX + 18, layout.contentY + 24, columnW, "Blue", String(snapshot.bluePoints), "Team Blue", true);
        drawMetric(p, layout.contentX + 28 + columnW, layout.contentY + 24, columnW, "Hand", String(snapshot.handCount), `Dealer ${hand.dealerName}`, true);
        drawMetric(p, layout.contentX + 38 + columnW * 2, layout.contentY + 24, columnW, "Red", String(snapshot.redPoints), "Team Red", true);

        const chipY = layout.contentY + 104;
        const chipW = (layout.contentW - 52) / 3;
        drawInfoChip(p, layout.contentX + 18, chipY, chipW, "Trump", hand.trump ? formatSuit(hand.trump) : "-");
        drawInfoChip(p, layout.contentX + 26 + chipW, chipY, chipW, "Leader", hand.leaderName ?? "-");
        drawInfoChip(p, layout.contentX + 34 + chipW * 2, chipY, chipW, "Caller", hand.callerName ?? "-");
        return;
    }

    drawMetric(p, layout.contentX + 28, layout.contentY + 28, 180, "Blue Team", String(snapshot.bluePoints), "Lance + Ephram", false);
    drawMetric(p, layout.contentX + layout.contentW / 2 - 120, layout.contentY + 28, 240, "Current Hand", `Hand ${snapshot.handCount}`, `Dealer ${hand.dealerName}  •  Tricks ${hand.blueTricks}-${hand.redTricks}`, false);
    drawMetric(p, layout.contentX + layout.contentW - 220, layout.contentY + 28, 180, "Red Team", String(snapshot.redPoints), "Laura + Olivia", false);

    const chipY = layout.contentY + 82;
    drawInfoChip(p, layout.contentX + 28, chipY, 130, "Trump", hand.trump ? formatSuit(hand.trump) : "-");
    drawInfoChip(p, layout.contentX + 170, chipY, 130, "Leader", hand.leaderName ?? "-");
    drawInfoChip(p, layout.contentX + 312, chipY, 130, "Caller", hand.callerName ?? "-");
}

function drawMetric(p, x, y, w, eyebrow, value, detail, compact) {
    p.fill(113, 96, 78);
    p.textSize(12);
    p.text(eyebrow.toUpperCase(), x, y);
    p.fill(33, 28, 24);
    p.textSize(compact ? 28 : 34);
    p.text(value, x, y + 34);
    p.fill(108, 90, 74);
    p.textSize(compact ? 12 : 15);
    p.text(detail, x, y + 58, w, compact ? 28 : 20);
}

function drawInfoChip(p, x, y, w, label, value) {
    p.fill(247, 241, 232);
    p.rect(x, y, w, 30, 15);
    p.fill(120, 102, 84);
    p.textSize(11);
    p.text(label.toUpperCase(), x + 12, y + 12);
    p.fill(33, 28, 24);
    p.textSize(14);
    p.text(value, x + 12, y + 24);
}

function drawTable(p, layout) {
    p.fill(33, 75, 54);
    p.rect(layout.tableX, layout.tableY, layout.tableW, layout.tableH, 30);
    p.fill(39, 96, 67);
    p.ellipse(layout.trickCenterX, layout.trickCenterY, layout.tableW * (layout.mobile ? 0.58 : 0.38), layout.tableH * (layout.mobile ? 0.28 : 0.34));
}

function drawSeat(p, player, hand, pendingAction, box, seat) {
    p.fill(255, 251, 246, seat === "south" ? 242 : 214);
    p.rect(box.x, box.y, box.w, box.h, 20);

    p.fill(112, 93, 75);
    p.textSize(12);
    p.text(player.isRemote ? "YOU" : "PLAYER", box.x + 14, box.y + 18);
    p.fill(34, 29, 24);
    p.textSize(box.h < 110 ? 18 : 22);
    p.text(player.name, box.x + 14, box.y + 44);
    p.fill(112, 93, 75);
    p.textSize(14);
    p.text(seatBadge(player, hand), box.x + 14, box.y + 64);

    if (seat === "south") {
        drawSouthHand(p, player, pendingAction, box);
        return;
    }

    if (seat === "north") {
        drawHorizontalBacks(p, player.handSize, box.x + Math.min(110, box.w * 0.36), box.y + 26, box.w - Math.min(126, box.w * 0.4), 18);
        return;
    }

    if (box.w < 160) {
        drawHorizontalBacks(p, player.handSize, box.x + 14, box.y + 30, box.w - 28, 14);
        return;
    }

    drawVerticalBacks(p, player.handSize, box.x + box.w - 82, box.y + 18, 14);
}

function drawSouthHand(p, player, pendingAction, box) {
    const cards = player.hand ?? [];
    const cardW = Math.max(42, Math.min(72, (box.w - 40) / (cards.length + Math.max(0, cards.length - 1) * 0.34)));
    const cardH = 104;
    const gap = Math.min(cardW * 0.78, Math.max(14, (box.w - 36 - cardW) / Math.max(1, cards.length - 1)));
    const startX = box.x + Math.max(14, (box.w - (cardW + gap * (cards.length - 1))) / 2);
    const y = box.y + box.h - Math.round(cardW * 1.45) - 14;
    const resolvedCardH = Math.round(cardW * 1.45);
    const legalIds = new Set(
        pendingAction && (pendingAction.type === "play_card" || pendingAction.type === "order_up")
            ? pendingAction.cards.map(card => card.id)
            : []
    );

    cards.forEach((card, index) => {
        const x = startX + gap * index;
        const isClickable = legalIds.has(card.id) && !state.submitting;
        const hovered = state.hoveredTarget?.value === card.id;
        const lift = hovered || isClickable ? 8 : 0;

        drawCardFace(p, x, y - lift, cardW, resolvedCardH, card, isClickable, hovered);
        if (isClickable) {
            addTarget({ x, y: y - lift, w: cardW, h: resolvedCardH, value: card.id });
        }
    });
}

function drawHorizontalBacks(p, count, x, y, maxWidth, overlap) {
    const cardW = Math.max(30, Math.min(56, maxWidth / Math.max(1.6, count * 0.72)));
    const cardH = Math.round(cardW * 1.46);
    const step = count <= 1 ? 0 : Math.min(cardW - overlap, (maxWidth - cardW) / (count - 1));
    for (let i = 0; i < count; i++) {
        drawCardBack(p, x + step * i, y, cardW, cardH);
    }
}

function drawVerticalBacks(p, count, x, y, overlap) {
    const cardW = 56;
    const cardH = 82;
    for (let i = 0; i < count; i++) {
        drawCardBack(p, x, y + (cardH - overlap) * 0.18 * i, cardW, cardH);
    }
}

function drawUpCard(p, card, box) {
    p.fill(255, 251, 246, 218);
    p.rect(box.x, box.y, box.w, box.h, 18);
    p.fill(112, 93, 75);
    p.textSize(12);
    p.text("UP CARD", box.x + 14, box.y + 20);
    if (card) {
        drawCardFace(p, box.x + 12, box.y + 28, box.w - 24, box.h - 40, card, false, false);
    }
}

function drawTrick(p, currentTrick, layout, players) {
    const trickCardW = layout.mobile ? 56 : 68;
    const trickCardH = layout.mobile ? 82 : 98;
    const trickBoxW = layout.mobile ? 190 : 220;
    const trickBoxH = layout.mobile ? 122 : 132;

    p.fill(245, 238, 226, 210);
    p.rect(layout.trickCenterX - trickBoxW / 2, layout.trickCenterY - trickBoxH / 2, trickBoxW, trickBoxH, 22);
    p.fill(245, 240, 233);
    p.textAlign(p.CENTER, p.CENTER);
    p.textSize(11);
    p.text("CURRENT TRICK", layout.trickCenterX, layout.trickCenterY - trickBoxH / 2 + 18);
    p.textAlign(p.LEFT, p.BASELINE);

    const positions = {
        0: { x: layout.trickCenterX - trickCardW / 2, y: layout.trickCenterY - (layout.mobile ? 94 : 112) },
        1: { x: layout.trickCenterX + (layout.mobile ? 34 : 42), y: layout.trickCenterY - trickCardH / 2 + 2 },
        2: { x: layout.trickCenterX - trickCardW / 2, y: layout.trickCenterY + (layout.mobile ? 18 : 26) },
        3: { x: layout.trickCenterX - (layout.mobile ? 92 : 110), y: layout.trickCenterY - trickCardH / 2 + 2 }
    };

    if (!currentTrick?.plays?.length) {
        p.fill(248, 243, 237);
        p.textAlign(p.CENTER, p.CENTER);
        p.textSize(16);
        p.text("Waiting for the lead", layout.trickCenterX, layout.trickCenterY + 6);
        p.textAlign(p.LEFT, p.BASELINE);
        return;
    }

    currentTrick.plays.forEach(play => {
        const cardPosition = positions[play.playerIdx];
        drawCardFace(p, cardPosition.x, cardPosition.y, trickCardW, trickCardH, play.card, false, false);
        p.fill(247, 242, 234);
        p.textAlign(p.CENTER, p.CENTER);
        p.textSize(layout.mobile ? 9 : 11);
        p.text(players[play.playerIdx].name, cardPosition.x + trickCardW / 2, cardPosition.y + trickCardH + 12);
        p.textAlign(p.LEFT, p.BASELINE);
    });
}

function drawActionStrip(p, snapshot, remotePlayer, layout) {
    const box = layout.actionBox;
    const pending = remotePlayer?.pendingAction;

    p.fill(255, 251, 246, 230);
    p.rect(box.x, box.y, box.w, box.h, 22);
    p.fill(111, 92, 74);
    p.textSize(12);
    p.text("ACTION", box.x + 18, box.y + 20);
    p.fill(35, 30, 24);
    p.textSize(20);
    p.textLeading(22);
    p.text(buildStatus(snapshot), box.x + 18, box.y + 44, box.w - 36, 48);

    if (!pending) {
        return;
    }

    if (pending.type === "order_up" && pending.canPass) {
        drawButton(p, box.x + box.w - 110, box.y + box.h - 52, 92, 38, "Pass", pending.passValue);
        return;
    }

    if (pending.type === "call_trump") {
        let cursorX = box.x + 18;
        let buttonY = box.y + 68;
        p.textSize(15);
        pending.suits.forEach(suit => {
            const label = formatSuit(suit);
            const buttonW = Math.max(88, p.textWidth(label) + 36);
            if (cursorX + buttonW > box.x + box.w - 18) {
                cursorX = box.x + 18;
                buttonY += 46;
            }
            drawButton(p, cursorX, buttonY, buttonW, 38, label, suit);
            cursorX += buttonW + 10;
        });
        if (pending.canPass) {
            if (cursorX + 88 > box.x + box.w - 18) {
                cursorX = box.x + 18;
                buttonY += 46;
            }
            drawButton(p, cursorX, buttonY, 88, 38, "Pass", pending.passValue);
        }
    }
}

function drawButton(p, x, y, w, h, label, value) {
    const hovered = state.hoveredTarget?.value === value;
    p.fill(hovered ? 237 : 248, hovered ? 231 : 244, hovered ? 222 : 238);
    p.stroke(124, 104, 84, 48);
    p.strokeWeight(1);
    p.rect(x, y, w, h, 18);
    p.noStroke();
    p.fill(33, 28, 24);
    p.textAlign(p.CENTER, p.CENTER);
    p.textSize(15);
    p.text(label, x + w / 2, y + h / 2 + 1);
    p.textAlign(p.LEFT, p.BASELINE);
    if (!state.submitting) {
        addTarget({ x, y, w, h, value });
    }
}

function drawCardFace(p, x, y, w, h, card, clickable, hovered) {
    p.fill(255, 255, 255);
    p.stroke(clickable ? 63 : 0, clickable ? 111 : 0, clickable ? 79 : 0, clickable ? 120 : 30);
    p.strokeWeight(clickable ? 2 : 1);
    p.rect(x, y, w, h, 14);
    p.noStroke();

    if (hovered) {
        p.fill(251, 247, 239, 120);
        p.rect(x + 2, y + 2, w - 4, h - 4, 12);
    }

    const color = isRed(card.suit) ? p.color(154, 44, 47) : p.color(31, 36, 44);
    p.fill(color);
    p.textSize(Math.max(15, Math.min(22, w * 0.24)));
    p.textAlign(p.LEFT, p.TOP);
    p.text(card.label, x + 10, y + 8);
    p.textAlign(p.CENTER, p.CENTER);
    p.textSize(Math.max(18, Math.min(34, w * 0.34)));
    p.text(card.label, x + w / 2, y + h / 2 + 4);
    p.textAlign(p.LEFT, p.BASELINE);
}

function drawCardBack(p, x, y, w, h) {
    p.fill(43, 95, 70);
    p.stroke(255, 255, 255, 44);
    p.strokeWeight(1);
    p.rect(x, y, w, h, 14);
    p.noStroke();
    p.fill(255, 255, 255, 44);
    for (let i = 0; i < 5; i++) {
        p.rect(x + 10 + i * 8, y + 10, 4, h - 20, 2);
    }
}
