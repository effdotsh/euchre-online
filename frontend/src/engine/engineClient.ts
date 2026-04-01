// AI-generated file: created by GitHub Copilot.
import type { ActionOption, GameSnapshot } from '../domain/types'

export type ConnectionStatus = 'connecting' | 'connected' | 'disconnected' | 'error'

type SnapshotListener = (snapshot: GameSnapshot) => void
type StatusListener = (status: ConnectionStatus) => void

interface SnapshotEnvelope {
  type: 'SNAPSHOT'
  snapshot: GameSnapshot
}

interface EventEnvelope {
  type: 'EVENT'
  eventType: string
  version: number
  tableId: string
  payload?: unknown
}

interface CommandEnvelope {
  type: 'COMMAND'
  tableId: string
  expectedVersion: number
  seat: number
  action: ActionOption
}

type ServerEnvelope = SnapshotEnvelope | EventEnvelope

class EngineClient {
  private readonly wsUrl: string

  private socket: WebSocket | null = null

  private snapshotListeners = new Set<SnapshotListener>()

  private statusListeners = new Set<StatusListener>()

  private latestSnapshot: GameSnapshot | null = null

  constructor(wsUrl: string) {
    this.wsUrl = wsUrl
  }

  connect(): void {
    if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
      return
    }

    this.emitStatus('connecting')

    this.socket = new WebSocket(this.wsUrl)

    this.socket.addEventListener('open', () => {
      this.emitStatus('connected')
    })

    this.socket.addEventListener('close', () => {
      this.emitStatus('disconnected')
    })

    this.socket.addEventListener('error', () => {
      this.emitStatus('error')
    })

    this.socket.addEventListener('message', (event) => {
      const envelope = this.parseEnvelope(event.data)
      if (!envelope) {
        return
      }

      if (envelope.type === 'SNAPSHOT') {
        this.latestSnapshot = envelope.snapshot
        this.emitSnapshot(envelope.snapshot)
      }
    })
  }

  disconnect(): void {
    if (!this.socket) {
      return
    }
    this.socket.close()
    this.socket = null
  }

  subscribeSnapshot(listener: SnapshotListener): () => void {
    this.snapshotListeners.add(listener)
    if (this.latestSnapshot) {
      listener(this.latestSnapshot)
    }
    return () => {
      this.snapshotListeners.delete(listener)
    }
  }

  subscribeStatus(listener: StatusListener): () => void {
    this.statusListeners.add(listener)
    return () => {
      this.statusListeners.delete(listener)
    }
  }

  sendAction(action: ActionOption, seat: number): void {
    if (!this.latestSnapshot) {
      return
    }

    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return
    }

    const envelope: CommandEnvelope = {
      type: 'COMMAND',
      tableId: this.latestSnapshot.tableId,
      expectedVersion: this.latestSnapshot.version,
      seat,
      action,
    }

    this.socket.send(JSON.stringify(envelope))
  }

  private parseEnvelope(rawData: unknown): ServerEnvelope | null {
    if (typeof rawData !== 'string') {
      return null
    }

    let parsed: unknown
    try {
      parsed = JSON.parse(rawData)
    } catch {
      return null
    }

    if (!parsed || typeof parsed !== 'object') {
      return null
    }

    const maybeEnvelope = parsed as Partial<ServerEnvelope>
    if (maybeEnvelope.type !== 'SNAPSHOT' && maybeEnvelope.type !== 'EVENT') {
      return null
    }

    return maybeEnvelope as ServerEnvelope
  }

  private emitSnapshot(snapshot: GameSnapshot): void {
    for (const listener of this.snapshotListeners) {
      listener(snapshot)
    }
  }

  private emitStatus(status: ConnectionStatus): void {
    for (const listener of this.statusListeners) {
      listener(status)
    }
  }
}

const wsUrl = import.meta.env.VITE_ENGINE_WS_URL ?? 'ws://localhost:8080/ws'

export const engineClient = new EngineClient(wsUrl)
