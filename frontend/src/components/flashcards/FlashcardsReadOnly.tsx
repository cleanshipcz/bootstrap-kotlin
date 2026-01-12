import type { FlashcardDto } from '@api/types'
import './FlashcardList.css'

interface FlashcardsReadOnlyProps {
  topicName: string
  flashcards: FlashcardDto[]
  onBack: () => void
  onEdit: () => void
}

export function FlashcardsReadOnly({ topicName, flashcards, onBack, onEdit }: FlashcardsReadOnlyProps) {
  return (
    <section className="flashcards-view fade-in">
      <div className="view-header">
        <button className="back-button" onClick={onBack}>
          ← Topic
        </button>
        <div>
          <p className="eyebrow">Flashcards</p>
          <h2>{topicName}</h2>
        </div>
        <button className="btn-secondary" onClick={onEdit}>
          Edit
        </button>
      </div>

      {flashcards.length === 0 ? (
        <div className="empty-state">
          <p>No flashcards yet.</p>
          <button className="btn-primary" onClick={onEdit}>
            Add your first card
          </button>
        </div>
      ) : (
        <div className="flashcards-stack">
          {flashcards.map((flashcard) => (
            <article key={flashcard.id ?? flashcard.question} className="flashcard-card">
              <div className="qa-block">
                <span className="label">Q:</span>
                <p>{flashcard.question}</p>
              </div>
              <div className="qa-block">
                <span className="label">A:</span>
                <p>{flashcard.answer}</p>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
