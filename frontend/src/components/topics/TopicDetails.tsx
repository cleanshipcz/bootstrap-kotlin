import { useCallback, useEffect, useRef, useState } from 'react'
import type { TopicDto } from '@api/types'
import { Modal } from '@components/ui/Modal'

interface TopicDetailsProps {
  topic: TopicDto | null
  onBack: () => void
  onViewFlashcards: () => void
  isLoading: boolean
  onUpdateStudyPassage: (passage: string) => Promise<void>
  onError: (message: string) => void
}

export function TopicDetails({
  topic,
  onBack,
  onViewFlashcards,
  isLoading,
  onUpdateStudyPassage,
  onError,
}: TopicDetailsProps) {
  const [isEditingNotes, setIsEditingNotes] = useState(false)
  const [draftNotes, setDraftNotes] = useState(topic?.studyPassage ?? '')
  const [notesModalOpen, setNotesModalOpen] = useState(false)
  const [notesSaving, setNotesSaving] = useState(false)
  const [pendingAction, setPendingAction] = useState<(() => void) | null>(null)
  const notesFieldRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    setDraftNotes(topic?.studyPassage ?? '')
  }, [topic?.studyPassage])

  const handleExecutePendingAction = () => {
    if (pendingAction) {
      pendingAction()
      setPendingAction(null)
    }
  }

  const handleAttemptExitNotes = useCallback(
    (nextAction?: () => void) => {
      if (!isEditingNotes) {
        nextAction?.()
        return
      }
      const baseline = topic?.studyPassage ?? ''
      const hasChanges = draftNotes !== baseline
      if (!hasChanges) {
        setIsEditingNotes(false)
        nextAction?.()
        return
      }
      setPendingAction(() => nextAction ?? null)
      setNotesModalOpen(true)
    },
    [draftNotes, isEditingNotes, topic?.studyPassage],
  )

  useEffect(() => {
    if (!isEditingNotes || notesModalOpen) {
      return
    }
    const handleMouseDown = (event: MouseEvent) => {
      if (notesFieldRef.current && notesFieldRef.current.contains(event.target as Node)) {
        return
      }
      handleAttemptExitNotes()
    }
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        event.preventDefault()
        handleAttemptExitNotes()
      }
    }
    document.addEventListener('mousedown', handleMouseDown)
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('mousedown', handleMouseDown)
      document.removeEventListener('keydown', handleKey)
    }
  }, [handleAttemptExitNotes, isEditingNotes, notesModalOpen])

  const beginEditingNotes = () => {
    if (!topic) {
      return
    }
    setDraftNotes(topic.studyPassage ?? '')
    setIsEditingNotes(true)
  }

  const handleBack = () => handleAttemptExitNotes(onBack)
  const handleStackNavigation = () => handleAttemptExitNotes(onViewFlashcards)

  const handleNotesModalSave = async () => {
    if (!topic) {
      return
    }
    setNotesSaving(true)
    try {
      await onUpdateStudyPassage(draftNotes)
      setIsEditingNotes(false)
      setNotesModalOpen(false)
      handleExecutePendingAction()
    } catch {
      onError('Unable to save study notes. Please try again.')
    } finally {
      setNotesSaving(false)
    }
  }

  const handleNotesModalDiscard = () => {
    setDraftNotes(topic?.studyPassage ?? '')
    setIsEditingNotes(false)
    setNotesModalOpen(false)
    handleExecutePendingAction()
  }

  const handleNotesModalCancel = () => {
    setPendingAction(null)
    setNotesModalOpen(false)
  }

  if (isLoading) {
    return (
      <section className="topic-detail fade-in">
        <p className="muted">Loading topic…</p>
      </section>
    )
  }

  if (!topic) {
    return (
      <section className="topic-detail fade-in">
        <p className="muted">Select a topic to view study material.</p>
      </section>
    )
  }

  const previewCards = (topic.flashcards ?? []).slice(0, 3)

  return (
    <section className="topic-detail fade-in">
      <button className="back-button" onClick={handleBack}>
        ← Topics
      </button>
      <header className="topic-header">
        <div>
          <p className="eyebrow">Topic</p>
          <h2>{topic.name}</h2>
          <p className="muted">{topic.flashcards?.length ?? 0} flashcards</p>
        </div>
      </header>

      <div
        className={`study-block ${isEditingNotes ? 'editing' : ''}`}
        onClick={!isEditingNotes ? beginEditingNotes : undefined}
        role={!isEditingNotes ? 'button' : undefined}
        tabIndex={!isEditingNotes ? 0 : -1}
        onKeyDown={(event) => {
          if (!isEditingNotes && (event.key === 'Enter' || event.key === ' ')) {
            event.preventDefault()
            beginEditingNotes()
          }
        }}
      >
        <div className="study-title">Study notes</div>
        <div ref={notesFieldRef} className="study-content">
          {isEditingNotes ? (
            <textarea
              value={draftNotes}
              onChange={(event) => setDraftNotes(event.target.value)}
              placeholder="Start typing your study notes…"
            />
          ) : (
            <p>{topic.studyPassage || 'No study passage provided yet.'}</p>
          )}
        </div>
      </div>

      <div className="stack-preview">
        <button type="button" className="stack-trigger" onClick={handleStackNavigation}>
          <div className="stack-cards">
            {previewCards.length === 0 && <div className="stack-card empty">No cards yet.</div>}
            {previewCards.map((card, index) => (
              <div key={card.id ?? `${card.question}-${index}`} className={`stack-card layer-${index}`}>
                <p className="stack-question">{card.question}</p>
              </div>
            ))}
          </div>
        </button>
      </div>

      {notesModalOpen && (
        <Modal
          title="Do you want to save your changes?"
          actions={[
            { label: 'Discard', onClick: handleNotesModalDiscard, variant: 'danger' },
            { label: 'Cancel', onClick: handleNotesModalCancel, variant: 'secondary' },
            { label: notesSaving ? 'Saving…' : 'Save', onClick: handleNotesModalSave, variant: 'primary', disabled: notesSaving },
          ]}
        />
      )}
    </section>
  )
}
