// AI-generated file: created by GitHub Copilot.
export type Suit = 'HEARTS' | 'DIAMONDS' | 'CLUBS' | 'SPADES'
export type Rank = '9' | '10' | 'J' | 'Q' | 'K' | 'A'

export type GamePhase =
  | 'HAND_START'
  | 'ORDER_UP'
  | 'CALL_TRUMP'
  | 'PLAY_TRICK'
  | 'TRICK_COMPLETE'
  | 'HAND_COMPLETE'

export interface CardView {
  suit: Suit
  rank: Rank
}

export interface SeatView {
  seat: number
  name: string
  isAi: boolean
  hand: CardView[]
}

export interface TeamScore {
  blue: number
  red: number
}

export type ActionOption =
  | {
      type: 'PLAY_CARD'
      card: CardView
      label: string
    }
  | {
      type: 'ORDER_UP'
      order: boolean
      alone?: boolean
      label: string
    }
  | {
      type: 'CALL_TRUMP'
      suit: Suit | null
      alone?: boolean
      label: string
    }

export interface GameSnapshot {
  tableId: string
  version: number
  phase: GamePhase
  statusText: string
  dealerSeat: number
  leaderSeat: number
  activeSeat: number
  callerSeat: number | null
  trump: Suit | null
  upCard: CardView | null
  seats: SeatView[]
  trickWinsBySeat: number[]
  trickCards: Array<CardView | null>
  handTricks: TeamScore
  gamePoints: TeamScore
  legalActions: ActionOption[]
}
