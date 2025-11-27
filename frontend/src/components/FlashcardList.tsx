import { useEffect, useState } from 'react'
import type { FlashcardDto } from '../api/types'
import './FlashcardList.css'

interface FlashcardListProps {
  topicName: string | null
  flashcards: FlashcardDto[]
  onCreateFlashcard: (question: string, answer: string) => void
  onDeleteFlashcard: (id: number) => void
  disabled: boolean
}

export function FlashcardList({
  topicName,
  flashcards,
  onCreateFlashcard,
  onDeleteFlashcard,
  disabled,
}: FlashcardListProps) {
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [question, setQuestion] = useState('')
  const [answer, setAnswer] = useState('')

  useEffect(() => {
    if (disabled) {
      setShowCreateForm(false)
      setQuestion('')
      setAnswer('')
    }
  }, [disabled])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (question.trim() && answer.trim()) {
      onCreateFlashcard(question.trim(), answer.trim())
      setQuestion('')
      setAnswer('')
      setShowCreateForm(false)
    }
  }

  return (
    <div className="flashcard-list">
      <div className="flashcard-list-header">
        <div>
          <h2>Flashcards</h2>
          <p className="muted">{topicName ? `Topic: ${topicName}` : 'Select a topic to manage flashcards.'}</p>
        </div>
        <button className="btn btn-primary" disabled={disabled} onClick={() => setShowCreateForm(true)}>
          + Add Flashcard
        </button>
      </div>

      {disabled && <p className="muted">Select a topic to view and create flashcards.</p>}

      {showCreateForm && (
        <form className="create-flashcard-form" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Question"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            autoFocus
          />
          <textarea
            placeholder="Answer"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            rows={3}
          />
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              Create
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setShowCreateForm(false)
                setQuestion('')
                setAnswer('')
              }}
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {flashcards.length === 0 ? (
        <div className="empty-state">
          <p>No flashcards yet. Add your first flashcard!</p>
        </div>
      ) : (
        <div className="flashcard-grid">
          {flashcards.map((flashcard) => (
            <div key={flashcard.id} className="flashcard-card">
              <button
                className="delete-btn"
                onClick={() => flashcard.id && onDeleteFlashcard(flashcard.id)}
                title="Delete flashcard"
              >
                ×
              </button>
              <div className="flashcard-question">
                <strong>Q:</strong> {flashcard.question}
              </div>
              <div className="flashcard-answer">
                <strong>A:</strong> {flashcard.answer}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

