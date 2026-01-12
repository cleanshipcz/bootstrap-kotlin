import { useCallback, useEffect, useMemo, useState } from 'react'
import { ApiError } from '@api/client'
import { flashcardsApi } from '@api/flashcards'
import { topicsApi } from '@api/topics'
import type { FlashcardDto } from '@api/types'
import { Modal } from '@components/ui/Modal'
import './FlashcardList.css'

const TrashIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" role="img" aria-hidden="true">
    <path d="M9 3h6l1 2h5v2H3V5h5l1-2zm2 6h2v9h-2zm-4 0h2v9H7zm8 0h2v9h-2z" fill="currentColor" />
  </svg>
)

const UndoIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" role="img" aria-hidden="true">
    <path
      d="M12 5H5v7h2.5V8.83l5.63 5.64a3.75 3.75 0 1 1-5.3 5.3l-1.77 1.77a6.25 6.25 0 1 0 8.84-8.84L11.17 7.5H15V5z"
      fill="currentColor"
    />
  </svg>
)

const RedoIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" role="img" aria-hidden="true">
    <path
      d="M12 5v2.5h3.83l-3.43 3.43A6.25 6.25 0 0 0 9.5 23.5 6.25 6.25 0 0 0 18.34 14l-1.77-1.77a3.75 3.75 0 1 1-5.3 5.3l5.63-5.64V12H19V5z"
      fill="currentColor"
    />
  </svg>
)

const PlusIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" role="img" aria-hidden="true">
    <path d="M11 11V5h2v6h6v2h-6v6h-2v-6H5v-2z" fill="currentColor" />
  </svg>
)

interface FlashcardsEditorProps {
  topicId: number
  topicName: string
  flashcards: FlashcardDto[]
  onClose: () => void
  onFlashcardsUpdated: (cards: FlashcardDto[]) => void
  onError: (message: string) => void
}

interface EditableCard {
  id: number | null
  key: string
  question: string
  answer: string
}

interface HistoryState {
  snapshots: EditableCard[][]
  index: number
}

const HISTORY_LIMIT = 20

const generateKey = () => {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `temp-${Math.random().toString(36).slice(2)}`
}

const cloneCards = (cards: EditableCard[]) => cards.map((card) => ({ ...card }))

export function FlashcardsEditor({
  topicId,
  topicName,
  flashcards,
  onClose,
  onFlashcardsUpdated,
  onError,
}: FlashcardsEditorProps) {
  const toEditable = useCallback(
    (cards: FlashcardDto[]): EditableCard[] =>
      cards.map((card) => ({
        id: card.id ?? null,
        key: card.id != null ? `card-${card.id}` : generateKey(),
        question: card.question,
        answer: card.answer,
      })),
    [],
  )

  const [history, setHistory] = useState<HistoryState>(() => ({
    snapshots: [toEditable(flashcards)],
    index: 0,
  }))
  const [baseline, setBaseline] = useState<EditableCard[]>(() => toEditable(flashcards))
  const [editingField, setEditingField] = useState<{ key: string; field: 'question' | 'answer' } | null>(null)
  const [showAddModal, setShowAddModal] = useState(false)
  const [newQuestion, setNewQuestion] = useState('')
  const [newAnswer, setNewAnswer] = useState('')
  const [addError, setAddError] = useState<string | null>(null)
  const [duplicateMessage, setDuplicateMessage] = useState<string | null>(null)
  const [validationMessage, setValidationMessage] = useState<string | null>(null)
  const [showExitConfirm, setShowExitConfirm] = useState(false)
  const [isSaving, setIsSaving] = useState(false)

  useEffect(() => {
    const initial = toEditable(flashcards)
    setHistory({ snapshots: [initial], index: 0 })
    setBaseline(initial)
  }, [flashcards, toEditable])

  const currentCards = history.snapshots[history.index] ?? []
  const canUndo = history.index > 0
  const canRedo = history.index < history.snapshots.length - 1

  const isDirty = useMemo(() => {
    if (currentCards.length !== baseline.length) {
      return true
    }
    return currentCards.some((card, index) => {
      const reference = baseline[index]
      if (!reference) {
        return true
      }
      const cardKey = card.id ?? card.key
      const refKey = reference.id ?? reference.key
      return cardKey !== refKey || card.question !== reference.question || card.answer !== reference.answer
    })
  }, [baseline, currentCards])

  const pushHistory = (nextCards: EditableCard[]) => {
    setHistory((prev) => {
      const truncated = prev.snapshots.slice(0, prev.index + 1)
      let snapshots = [...truncated, cloneCards(nextCards)]
      if (snapshots.length > HISTORY_LIMIT) {
        snapshots = snapshots.slice(snapshots.length - HISTORY_LIMIT)
      }
      return { snapshots, index: snapshots.length - 1 }
    })
  }

  const handleUndo = () => {
    if (!canUndo) {
      return
    }
    setHistory((prev) => ({ ...prev, index: prev.index - 1 }))
  }

  const handleRedo = () => {
    if (!canRedo) {
      return
    }
    setHistory((prev) => ({ ...prev, index: prev.index + 1 }))
  }

  const updateCardField = (cardKey: string, field: 'question' | 'answer', value: string) => {
    setEditingField({ key: cardKey, field })
    const next = currentCards.map((card) => (card.key === cardKey ? { ...card, [field]: value } : card))
    pushHistory(next)
  }

  const handleDeleteCard = (cardKey: string) => {
    const next = currentCards.filter((card) => card.key !== cardKey)
    pushHistory(next)
    if (editingField?.key === cardKey) {
      setEditingField(null)
    }
  }

  const hasDuplicateQuestion = (question: string, excludeKey?: string) => {
    const normalized = question.trim().toLowerCase()
    return currentCards.some(
      (card) => card.key !== excludeKey && card.question.trim().toLowerCase() === normalized,
    )
  }

  const closeAddModal = () => {
    setShowAddModal(false)
    setAddError(null)
    setNewQuestion('')
    setNewAnswer('')
  }

  const openAddModal = () => {
    setNewQuestion('')
    setNewAnswer('')
    setAddError(null)
    setShowAddModal(true)
  }

  const handleAddCard = () => {
    const trimmedQuestion = newQuestion.trim()
    const trimmedAnswer = newAnswer.trim()
    if (!trimmedQuestion || !trimmedAnswer) {
      setAddError('Question and answer are required.')
      return
    }
    if (hasDuplicateQuestion(trimmedQuestion)) {
      setDuplicateMessage('A card with this question already exists in this topic.')
      return
    }
    const nextCard: EditableCard = {
      id: null,
      key: generateKey(),
      question: trimmedQuestion,
      answer: trimmedAnswer,
    }
    pushHistory([...currentCards, nextCard])
    closeAddModal()
  }

  const findDuplicateInCurrent = () => {
    const seen = new Set<string>()
    for (const card of currentCards) {
      const normalized = card.question.trim().toLowerCase()
      if (!normalized) {
        continue
      }
      if (seen.has(normalized)) {
        return card.question
      }
      seen.add(normalized)
    }
    return null
  }

  const validateBeforeSave = () => {
    const duplicate = findDuplicateInCurrent()
    if (duplicate) {
      setDuplicateMessage('A card with this question already exists in this topic.')
      return false
    }
    const hasEmptyFields = currentCards.some((card) => !card.question.trim() || !card.answer.trim())
    if (hasEmptyFields) {
      setValidationMessage('Please fill in both the question and answer for every flashcard.')
      return false
    }
    return true
  }

  const syncFromServer = async () => {
    const refreshed = await topicsApi.getOne(topicId)
    onFlashcardsUpdated(refreshed.flashcards)
    const nextEditable = toEditable(refreshed.flashcards)
    setHistory({ snapshots: [nextEditable], index: 0 })
    setBaseline(nextEditable)
  }

  const handleSave = async () => {
    if (!validateBeforeSave()) {
      return
    }
    setIsSaving(true)
    try {
      const baselineMap = new Map(baseline.filter((card) => card.id != null).map((card) => [card.id as number, card]))
      const currentMap = new Map(
        currentCards.filter((card) => card.id != null).map((card) => [card.id as number, card]),
      )

      const toDelete = baseline.filter((card) => card.id != null && !currentMap.has(card.id))
      const toCreate = currentCards.filter((card) => card.id == null)
      const toUpdate = currentCards.filter((card) => {
        if (card.id == null) {
          return false
        }
        const reference = baselineMap.get(card.id)
        if (!reference) {
          return false
        }
        return reference.question !== card.question || reference.answer !== card.answer
      })

      for (const card of toDelete) {
        await flashcardsApi.delete(topicId, card.id!)
      }
      for (const card of toUpdate) {
        await flashcardsApi.update(topicId, card.id!, {
          question: card.question.trim(),
          answer: card.answer.trim(),
        })
      }
      for (const card of toCreate) {
        await flashcardsApi.create(topicId, {
          question: card.question.trim(),
          answer: card.answer.trim(),
        })
      }

      await syncFromServer()
      setShowExitConfirm(false)
      onClose()
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        setDuplicateMessage('A card with this question already exists in this topic.')
      } else {
        onError('Failed to save flashcards. Please try again.')
      }
    } finally {
      setIsSaving(false)
    }
  }

  const requestExit = () => {
    if (isDirty) {
      setShowExitConfirm(true)
    } else {
      onClose()
    }
  }

  const discardChanges = () => {
    setHistory({ snapshots: [cloneCards(baseline)], index: 0 })
    setShowExitConfirm(false)
    onClose()
  }

  const renderField = (card: EditableCard, field: 'question' | 'answer') => {
    const isActive = editingField?.key === card.key && editingField.field === field
    if (isActive) {
      return (
        <textarea
          value={card[field]}
          onChange={(event) => updateCardField(card.key, field, event.target.value)}
          onBlur={() => setEditingField(null)}
          autoFocus
          rows={field === 'question' ? 2 : 3}
        />
      )
    }
    const text = card[field]
    return (
      <button type="button" className="plain-field" onClick={() => setEditingField({ key: card.key, field })}>
        {text ? text : <span className="muted">Tap to edit {field}</span>}
      </button>
    )
  }

  return (
    <section className="flashcards-editor fade-in">
      <div className="editor-toolbar">
        <button className="back-button" onClick={requestExit}>
          ← Back
        </button>
        <div className="toolbar-title">
          <p className="eyebrow">Editing</p>
          <h2>{topicName}</h2>
        </div>
        <div className="toolbar-actions">
          <button className="ghost-button icon-only" onClick={handleUndo} disabled={!canUndo} aria-label="Undo">
            <UndoIcon />
          </button>
          <button className="ghost-button icon-only" onClick={handleRedo} disabled={!canRedo} aria-label="Redo">
            <RedoIcon />
          </button>
          <button className="ghost-button icon-only" onClick={openAddModal} aria-label="Add flashcard">
            <PlusIcon />
          </button>
          <button className="btn-primary" onClick={handleSave} disabled={isSaving || !isDirty}>
            Save
          </button>
        </div>
      </div>

      <div className="editable-list">
        {currentCards.length === 0 && <p className="muted">No flashcards yet. Use the + button to add one.</p>}
        {currentCards.map((card, index) => (
          <article key={card.key} className="editable-card">
            <div className="card-head">
              <span className="chip">#{index + 1}</span>
              <button className="icon-button" onClick={() => handleDeleteCard(card.key)} aria-label="Delete flashcard">
                <TrashIcon />
              </button>
            </div>
            <div className="editable-field">
              <span className="label">Q</span>
              {renderField(card, 'question')}
            </div>
            <div className="editable-field">
              <span className="label">A</span>
              {renderField(card, 'answer')}
            </div>
          </article>
        ))}
      </div>

      {showAddModal && (
        <Modal
          title="Add flashcard"
          actions={[
            { label: 'Cancel', onClick: closeAddModal, variant: 'secondary' },
            { label: 'Add', onClick: handleAddCard, variant: 'primary' },
          ]}
        >
          <div className="form-row">
            <label htmlFor="new-question">Question</label>
            <input
              id="new-question"
              type="text"
              value={newQuestion}
              onChange={(event) => setNewQuestion(event.target.value)}
              autoFocus
            />
          </div>
          <div className="form-row">
            <label htmlFor="new-answer">Answer</label>
            <textarea
              id="new-answer"
              rows={3}
              value={newAnswer}
              onChange={(event) => setNewAnswer(event.target.value)}
            />
          </div>
          {addError && <p className="error-text">{addError}</p>}
        </Modal>
      )}

      {duplicateMessage && (
        <Modal
          title="Duplicate question"
          description={duplicateMessage}
          actions={[{ label: 'Close', onClick: () => setDuplicateMessage(null), variant: 'primary' }]}
        />
      )}

      {validationMessage && (
        <Modal
          title="Incomplete flashcard"
          description={validationMessage}
          actions={[{ label: 'Close', onClick: () => setValidationMessage(null), variant: 'primary' }]}
        />
      )}

      {showExitConfirm && (
        <Modal
          title="Save changes?"
          description="Do you want to save your changes or discard them?"
          actions={[
            { label: 'Discard', onClick: discardChanges, variant: 'danger' },
            { label: 'Cancel', onClick: () => setShowExitConfirm(false), variant: 'secondary' },
            { label: 'Save', onClick: handleSave, variant: 'primary' },
          ]}
        />
      )}
    </section>
  )
}
