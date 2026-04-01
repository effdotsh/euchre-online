// AI-generated file: created by GitHub Copilot.
import { useMemo } from 'react'
import type { ActionOption, CardView } from './domain/types'
import { useGameFeed } from './hooks/useGameFeed'
import { PixiTable } from './ui/PixiTable'
import './App.css'

const LOCAL_SEAT = 2

function App() {
  const { snapshot, status, sendAction } = useGameFeed(LOCAL_SEAT)

  if (!snapshot) {
    return (
      <main className="app-shell">
        <aside className="control-panel">
          <h1>Euchre UI</h1>
          <p className="subhead">Waiting for observer snapshot stream from the Java engine.</p>
          <section className="panel-block">
            <h2>Connection</h2>
            <p>Status: {status}</p>
            <p>WebSocket: {import.meta.env.VITE_ENGINE_WS_URL ?? 'ws://localhost:8080/ws'}</p>
          </section>
        </aside>

        <section className="table-stage">
          <div className="table-frame">
            <div className="engine-waiting">No snapshot received yet.</div>
          </div>
        </section>
      </main>
    )
  }

  const localPlayer = snapshot.seats[LOCAL_SEAT]
  const actionButtons = useMemo(
    () => (snapshot.activeSeat === LOCAL_SEAT ? snapshot.legalActions : []),
    [snapshot.activeSeat, snapshot.legalActions],
  )

  const cardPlayActions = useMemo(
    () => actionButtons.filter((action) => action.type === 'PLAY_CARD'),
    [actionButtons],
  )

  const legalPlayableCards = useMemo(
    () => cardPlayActions.map((action) => action.card),
    [cardPlayActions],
  )

  const nonCardActions = useMemo(
    () => actionButtons.filter((action) => action.type !== 'PLAY_CARD'),
    [actionButtons],
  )

  const showBiddingModal = useMemo(
    () =>
      snapshot.activeSeat === LOCAL_SEAT &&
      (snapshot.phase === 'ORDER_UP' || snapshot.phase === 'CALL_TRUMP') &&
      nonCardActions.length > 0,
    [snapshot.activeSeat, snapshot.phase, nonCardActions],
  )

  const biddingTitle = snapshot.phase === 'ORDER_UP' ? 'Order Up Decision' : 'Call Trump Decision'
  const upCardLabel = snapshot.upCard ? `${snapshot.upCard.rank} of ${snapshot.upCard.suit}` : 'None'

  const canAct = snapshot.activeSeat === LOCAL_SEAT && actionButtons.length > 0
  const canPlayCards = snapshot.activeSeat === LOCAL_SEAT && snapshot.phase === 'PLAY_TRICK'

  const onAction = (action: ActionOption) => {
    if (!canAct) {
      return
    }
    sendAction(action)
  }

  const onCardPlay = (card: CardView) => {
    if (!canPlayCards) {
      return
    }
    sendAction({
      type: 'PLAY_CARD',
      card,
      label: `${card.rank} of ${card.suit}`,
    })
  }

  return (
    <main className="app-shell">
      <aside className="control-panel">
        <h1>Euchre UI Sandbox</h1>
        <p className="subhead">Renderer-only UI driven by observer snapshots from the Java engine.</p>

        <section className="panel-block">
          <h2>Perspective Seat</h2>
          <p>Locked to South seat for now.</p>
        </section>

        <section className="panel-block">
          <h2>Snapshot</h2>
          <p>Version: {snapshot.version}</p>
          <p>Phase: {snapshot.phase}</p>
          <p>Local player: {localPlayer.name}</p>
          <p>Active seat: {snapshot.seats[snapshot.activeSeat].name}</p>
          <p>Trump: {snapshot.trump ?? 'None'}</p>
          <p>Status: {snapshot.statusText}</p>
        </section>

        <section className="panel-block">
          <h2>Tricks This Round</h2>
          {snapshot.seats.map((seat) => (
            <p key={seat.seat}>
              {seat.name} ({seat.seat}): {snapshot.trickWinsBySeat[seat.seat]}
            </p>
          ))}
        </section>

        <section className="panel-block">
          <h2>Available Actions</h2>
          <div className="actions-list">
            {canPlayCards && <p className="muted">Play a highlighted card directly from your hand.</p>}
            {showBiddingModal && <p className="muted">Use the bidding popup on the table to choose.</p>}
            {!showBiddingModal &&
              nonCardActions.map((action, index) => (
                <button key={`${action.label}-${index}`} className="action-button" type="button" onClick={() => onAction(action)}>
                  {action.label}
                </button>
              ))}
            {!canAct && <p className="muted">Waiting for other seats...</p>}
          </div>
        </section>
      </aside>

      <section className="table-stage">
        <div className="table-frame">
          <PixiTable
            snapshot={snapshot}
            localSeat={LOCAL_SEAT}
            legalPlayableCards={legalPlayableCards}
            canPlayCards={canPlayCards}
            onCardPlay={onCardPlay}
          />

          {showBiddingModal && (
            <div className="bidding-modal-backdrop">
              <div className="bidding-modal" role="dialog" aria-modal="true" aria-label="Bidding Decision">
                <h3>{biddingTitle}</h3>
                {snapshot.phase === 'ORDER_UP' && (
                  <p className="upcard-preview">
                    Turned card: <strong>{upCardLabel}</strong>
                  </p>
                )}
                <p className="bidding-subtitle">
                  {snapshot.phase === 'ORDER_UP'
                    ? 'Choose whether to order up the turned card, pass, or go alone.'
                    : 'Choose a valid suit (not the turned-up suit), with optional go alone.'}
                </p>
                <div className="bidding-actions">
                  {nonCardActions.map((action, index) => (
                    <button
                      key={`${action.label}-${index}`}
                      className="bidding-action-button"
                      type="button"
                      onClick={() => onAction(action)}
                    >
                      {action.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>
        <div className="legend-row">
          <span className="chip">Blue team: seats 0 and 2</span>
          <span className="chip">Red team: seats 1 and 3</span>
            <span className="chip">UI sends commands; engine is source of truth</span>
        </div>
      </section>
    </main>
  )
}

export default App
