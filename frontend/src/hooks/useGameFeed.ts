// AI-generated file: created by GitHub Copilot.
import { useCallback, useEffect, useState } from 'react'
import type { ActionOption, GameSnapshot } from '../domain/types'
import { engineClient, type ConnectionStatus } from '../engine/engineClient'

export function useGameFeed(localSeat: number): {
  snapshot: GameSnapshot | null
  status: ConnectionStatus
  sendAction: (action: ActionOption) => void
} {
  const [snapshot, setSnapshot] = useState<GameSnapshot | null>(null)
  const [status, setStatus] = useState<ConnectionStatus>('connecting')

  useEffect(() => {
    const unsubscribeSnapshot = engineClient.subscribeSnapshot(setSnapshot)
    const unsubscribeStatus = engineClient.subscribeStatus(setStatus)

    engineClient.connect()

    return () => {
      unsubscribeSnapshot()
      unsubscribeStatus()
    }
  }, [])

  const sendAction = useCallback(
    (action: ActionOption) => {
      engineClient.sendAction(action, localSeat)
    },
    [localSeat],
  )

  return { snapshot, status, sendAction }
}
