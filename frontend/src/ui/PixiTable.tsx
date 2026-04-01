// AI-generated file: created by GitHub Copilot.
import { useEffect, useRef } from 'react'
import { Application, Container, Graphics, Text, TextStyle } from 'pixi.js'
import type { CardView, GameSnapshot } from '../domain/types'

interface PixiTableProps {
  snapshot: GameSnapshot
  localSeat: number
  legalPlayableCards: CardView[]
  canPlayCards: boolean
  onCardPlay: (card: CardView) => void
}

const CARD_WIDTH = 72
const CARD_HEIGHT = 108
const CARD_RADIUS = 10
const HAND_CARD_GAP = 76
const HAND_WIDTH = CARD_WIDTH + HAND_CARD_GAP * 4

function suitColor(card: CardView): number {
  return card.suit === 'HEARTS' || card.suit === 'DIAMONDS' ? 0xb3252c : 0x1c2028
}

function cardKey(card: CardView): string {
  return `${card.rank}-${card.suit}`
}

function drawSuitSymbol(card: CardView, x: number, y: number, size: number, stage: Container): void {
  const color = suitColor(card)

  if (card.suit === 'SPADES') {
    const symbol = new Text({
      text: '♠',
      style: new TextStyle({
        fill: color,
        fontFamily: 'Georgia, Times New Roman, serif',
        fontSize: Math.round(size * 2.3),
        fontWeight: '700',
      }),
    })
    symbol.x = x - symbol.width / 2
    symbol.y = y - symbol.height * 0.64
    stage.addChild(symbol)
    return
  }

  const shape = new Graphics()

  if (card.suit === 'HEARTS') {
    shape.circle(x - size * 0.4, y - size * 0.2, size * 0.38)
    shape.fill(color)
    shape.circle(x + size * 0.4, y - size * 0.2, size * 0.38)
    shape.fill(color)
    shape.poly([
      x - size * 0.8,
      y - size * 0.05,
      x + size * 0.8,
      y - size * 0.05,
      x,
      y + size,
    ])
    shape.fill(color)
  } else if (card.suit === 'DIAMONDS') {
    shape.poly([
      x,
      y - size,
      x + size * 0.72,
      y,
      x,
      y + size,
      x - size * 0.72,
      y,
    ])
    shape.fill(color)
  } else if (card.suit === 'CLUBS') {
    shape.circle(x, y - size * 0.45, size * 0.38)
    shape.fill(color)
    shape.circle(x - size * 0.45, y + size * 0.02, size * 0.38)
    shape.fill(color)
    shape.circle(x + size * 0.45, y + size * 0.02, size * 0.38)
    shape.fill(color)
    shape.roundRect(x - size * 0.13, y + size * 0.2, size * 0.26, size * 0.72, size * 0.12)
    shape.fill(color)
  }

  stage.addChild(shape)
}

function drawCard(
  card: CardView,
  hidden: boolean,
  x: number,
  y: number,
  stage: Container,
  options?: {
    greyedOut?: boolean
    clickable?: boolean
    onClick?: () => void
  },
): void {
  const cardRect = new Graphics()
  const greyedOut = options?.greyedOut ?? false
  const clickable = options?.clickable ?? false

  cardRect.roundRect(x, y, CARD_WIDTH, CARD_HEIGHT, CARD_RADIUS)
  cardRect.fill(hidden ? 0x2f4f6f : greyedOut ? 0xd6d8dc : 0xf6f2e8)
  cardRect.stroke({ width: 2, color: hidden ? 0x7ca4d3 : 0x10222f })

  if (!hidden && clickable && options?.onClick) {
    cardRect.eventMode = 'static'
    cardRect.cursor = 'pointer'
    cardRect.on('pointertap', options.onClick)
  }

  stage.addChild(cardRect)

  if (!hidden) {
    const corner = new Text({
      text: card.rank,
      style: new TextStyle({
        fill: greyedOut ? 0x6f737a : suitColor(card),
        fontFamily: 'Georgia',
        fontSize: 20,
        fontWeight: '700',
      }),
    })
    corner.x = x + 10
    corner.y = y + 8
    stage.addChild(corner)

    if (greyedOut) {
      const symbol = new Text({
        text: card.suit === 'SPADES' ? '♠' : card.suit === 'HEARTS' ? '♥' : card.suit === 'DIAMONDS' ? '♦' : '♣',
        style: new TextStyle({
          fill: 0x747981,
          fontFamily: 'Georgia, Times New Roman, serif',
          fontSize: 34,
          fontWeight: '700',
        }),
      })
      symbol.x = x + CARD_WIDTH / 2 - symbol.width / 2
      symbol.y = y + CARD_HEIGHT / 2 - symbol.height / 2
      stage.addChild(symbol)
    } else {
      drawSuitSymbol(card, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2, 15, stage)
    }
  }
}

function renderScene(
  app: Application,
  snapshot: GameSnapshot,
  localSeat: number,
  legalPlayableCards: CardView[],
  canPlayCards: boolean,
  onCardPlay: (card: CardView) => void,
): void {
  app.stage.removeChildren()

  const width = app.screen.width
  const height = app.screen.height

  const background = new Graphics()
  background.rect(0, 0, width, height)
  background.fill(0x0f2d28)
  app.stage.addChild(background)

  const felt = new Graphics()
  felt.roundRect(24, 24, width - 48, height - 48, 22)
  felt.fill(0x1d5e4f)
  felt.stroke({ width: 4, color: 0x0a201c })
  app.stage.addChild(felt)

  const title = new Text({
    text: `Table ${snapshot.tableId} - ${snapshot.phase}`,
    style: new TextStyle({
      fill: 0xf1f4e8,
      fontFamily: 'Trebuchet MS',
      fontSize: 20,
      fontWeight: '700',
    }),
  })
  title.x = 38
  title.y = 34
  app.stage.addChild(title)

  const score = new Text({
    text: `Blue ${snapshot.gamePoints.blue} | Red ${snapshot.gamePoints.red}   Tricks ${snapshot.handTricks.blue}-${snapshot.handTricks.red}`,
    style: new TextStyle({
      fill: 0xe6efe8,
      fontFamily: 'Trebuchet MS',
      fontSize: 16,
      fontWeight: '600',
    }),
  })
  score.x = 38
  score.y = 64
  app.stage.addChild(score)

  const status = new Text({
    text: snapshot.statusText,
    style: new TextStyle({
      fill: 0xf7f0d0,
      fontFamily: 'Trebuchet MS',
      fontSize: 16,
      fontStyle: 'italic',
    }),
  })
  status.x = 38
  status.y = height - 52
  app.stage.addChild(status)

  const centeredHandX = width / 2 - HAND_WIDTH / 2
  const trickY = height / 2 - CARD_HEIGHT / 2
  const sideHandX = {
    west: 40,
    east: width - HAND_WIDTH - 40,
  }

  const northSouthY = {
    north: 96,
    south: height - 160,
  }

  const seatPositions = [
    { x: centeredHandX, y: northSouthY.north },
    { x: sideHandX.east, y: trickY },
    { x: centeredHandX, y: northSouthY.south },
    { x: sideHandX.west, y: trickY },
  ]

  const legalPlayableSet = new Set(legalPlayableCards.map(cardKey))

  for (let seat = 0; seat < snapshot.seats.length; seat += 1) {
    const seatState = snapshot.seats[seat]
    const position = seatPositions[seat]
    const tricksTaken = snapshot.trickWinsBySeat[seat] ?? 0
    const isActive = snapshot.activeSeat === seat
    const isBlueTeam = seat % 2 === 0

    const hudFill = isBlueTeam ? 0x16394f : 0x4a2626
    const hudBorder = isBlueTeam ? 0x4c8fbc : 0xb56f6f
    const activeHudBorder = isBlueTeam ? 0xf2dc78 : 0xf2cf6e
    const tricksBoxFill = isBlueTeam ? 0x225774 : 0x6a3737
    const tricksBoxBorder = isBlueTeam ? 0x72abc9 : 0xc58989
    const nameColor = isBlueTeam ? 0xe6f3fb : 0xffece6
    const tricksColor = isBlueTeam ? 0xeaf6ff : 0xffefcf

    const hudX = position.x
    const hudY = position.y - 42
    const hudWidth = 248
    const hudHeight = 30
    const tricksBoxWidth = 42

    const hud = new Graphics()
    hud.roundRect(hudX, hudY, hudWidth, hudHeight, 8)
    hud.fill(hudFill)
    hud.stroke({ width: 2, color: isActive ? activeHudBorder : hudBorder })
    app.stage.addChild(hud)

    const tricksBox = new Graphics()
    tricksBox.roundRect(hudX + hudWidth - tricksBoxWidth - 6, hudY + 4, tricksBoxWidth, hudHeight - 8, 6)
    tricksBox.fill(tricksBoxFill)
    tricksBox.stroke({ width: 1, color: tricksBoxBorder })
    app.stage.addChild(tricksBox)

    const nameLabel = new Text({
      text: `P${seat + 1} ${seatState.name}${seatState.isAi ? ' (AI)' : ''}`,
      style: new TextStyle({
        fill: nameColor,
        fontFamily: 'Trebuchet MS',
        fontSize: 14,
        fontWeight: '600',
      }),
    })
    nameLabel.x = hudX + 10
    nameLabel.y = hudY + 7
    app.stage.addChild(nameLabel)

    const tricksLabel = new Text({
      text: `${tricksTaken}`,
      style: new TextStyle({
        fill: tricksColor,
        fontFamily: 'Trebuchet MS',
        fontSize: 16,
        fontWeight: '700',
      }),
    })
    tricksLabel.x = hudX + hudWidth - tricksBoxWidth + 10
    tricksLabel.y = hudY + 6
    app.stage.addChild(tricksLabel)

    if (isActive) {
      const turnIndicator = new Graphics()
      turnIndicator.poly([
        hudX + hudWidth - 16,
        hudY + 2,
        hudX + hudWidth - 2,
        hudY + 2,
        hudX + hudWidth - 2,
        hudY + 16,
      ])
      turnIndicator.fill(0xf5d25a)
      app.stage.addChild(turnIndicator)
    }

    seatState.hand.forEach((card, cardIndex) => {
      const x = position.x + cardIndex * HAND_CARD_GAP
      const hidden = seat !== localSeat

      const isPlayable =
        !hidden &&
        canPlayCards &&
        legalPlayableSet.has(cardKey(card))

      const shouldGreyOut = !hidden && !isPlayable

      drawCard(card, hidden, x, position.y, app.stage, {
        greyedOut: shouldGreyOut,
        clickable: isPlayable,
        onClick: isPlayable ? () => onCardPlay(card) : undefined,
      })
    })
  }

  const trickX = width / 2 - (CARD_WIDTH * 4 + 18 * 3) / 2
  snapshot.trickCards.forEach((card, seat) => {
    if (!card) {
      return
    }
    drawCard(card, false, trickX + seat * (CARD_WIDTH + 18), trickY, app.stage)
  })

  if (snapshot.upCard !== null && (snapshot.phase === 'ORDER_UP' || snapshot.phase === 'CALL_TRUMP')) {
    const upCard: CardView = snapshot.upCard
    drawCard(upCard, false, width / 2 - CARD_WIDTH / 2, height / 2 - CARD_HEIGHT / 2, app.stage)
    const upText = new Text({
      text: 'Up Card',
      style: new TextStyle({
        fill: 0xecf5ef,
        fontFamily: 'Trebuchet MS',
        fontSize: 14,
      }),
    })
    upText.x = width / 2 - 24
    upText.y = height / 2 - CARD_HEIGHT / 2 - 22
    app.stage.addChild(upText)
  }

  const trumpText = new Text({
    text: `Trump: ${snapshot.trump ?? 'None'}`,
    style: new TextStyle({
      fill: 0xffefb4,
      fontFamily: 'Trebuchet MS',
      fontSize: 16,
      fontWeight: '700',
    }),
  })
  trumpText.x = width - 220
  trumpText.y = 34
  app.stage.addChild(trumpText)
}

export function PixiTable({ snapshot, localSeat, legalPlayableCards, canPlayCards, onCardPlay }: PixiTableProps) {
  const hostRef = useRef<HTMLDivElement | null>(null)
  const appRef = useRef<Application | null>(null)

  useEffect(() => {
    let cancelled = false

    const mount = async () => {
      const host = hostRef.current
      if (!host || cancelled) {
        return
      }

      const app = new Application()
      await app.init({
        width: host.clientWidth || 960,
        height: 620,
        antialias: true,
        backgroundAlpha: 0,
      })

      if (cancelled) {
        app.destroy(true)
        return
      }

      host.innerHTML = ''
      host.appendChild(app.canvas)
      appRef.current = app
      renderScene(app, snapshot, localSeat, legalPlayableCards, canPlayCards, onCardPlay)

      const onResize = () => {
        const nextWidth = host.clientWidth || 960
        app.renderer.resize(nextWidth, 620)
        renderScene(app, snapshot, localSeat, legalPlayableCards, canPlayCards, onCardPlay)
      }

      window.addEventListener('resize', onResize)
      ;(app as unknown as { __cleanup?: () => void }).__cleanup = () => {
        window.removeEventListener('resize', onResize)
      }
    }

    void mount()

    return () => {
      cancelled = true
      const app = appRef.current
      if (app) {
        const cleanup = (app as unknown as { __cleanup?: () => void }).__cleanup
        cleanup?.()
        app.destroy(true, { children: true, texture: true })
      }
      appRef.current = null
    }
  }, [])

  useEffect(() => {
    const app = appRef.current
    if (!app) {
      return
    }
    renderScene(app, snapshot, localSeat, legalPlayableCards, canPlayCards, onCardPlay)
  }, [snapshot, localSeat, legalPlayableCards, canPlayCards, onCardPlay])

  return <div className="pixi-host" ref={hostRef} />
}
