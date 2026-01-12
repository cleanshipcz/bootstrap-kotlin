import { useState } from 'react'
import type { TopicDto } from '@api/types'

interface TopicListProps {
  subjectName: string
  topics: TopicDto[]
  selectedTopicId: number | null
  onSelectTopic: (topicId: number) => void
  onCreateTopic: (name: string, studyPassage: string) => Promise<void> | void
  passageLimit: number
  isLoading: boolean
  onBack: () => void
}

export function TopicList({
  subjectName,
  topics,
  selectedTopicId,
  onSelectTopic,
  onCreateTopic,
  passageLimit,
  isLoading,
  onBack,
}: TopicListProps) {
  const [name, setName] = useState('')
  const [passage, setPassage] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    const trimmedName = name.trim()
    const trimmedPassage = passage.trim()
    if (!trimmedName || !trimmedPassage) {
      return
    }
    setIsSubmitting(true)
    await onCreateTopic(trimmedName, trimmedPassage)
    setIsSubmitting(false)
    setName('')
    setPassage('')
    setShowForm(false)
  }

  return (
    <section className="topics-view fade-in">
      <div className="view-header">
        <button className="back-button" onClick={onBack}>
          ← Subjects
        </button>
        <div>
          <p className="eyebrow">Subject</p>
          <h2>{subjectName}</h2>
        </div>
        <button className="ghost-button" onClick={() => setShowForm(true)}>
          Add Topic
        </button>
      </div>

      {isLoading ? (
        <p className="muted">Loading topics…</p>
      ) : (
        <ul className="topics-list">
          {topics.length === 0 && <li className="muted">No topics yet. Add one to continue.</li>}
          {topics.map((topic) => (
            <li key={topic.id ?? topic.name}>
              <button
                className={`topic-card ${selectedTopicId === topic.id ? 'selected' : ''}`}
                onClick={() => topic.id && onSelectTopic(topic.id)}
              >
                <div>
                  <h3>{topic.name}</h3>
                  <p>{topic.flashcardCount ?? topic.flashcards?.length ?? 0} flashcards</p>
                </div>
                <span className="chevron">›</span>
              </button>
            </li>
          ))}
        </ul>
      )}

      {showForm && (
        <form className="topic-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <label htmlFor="topic-name">Topic name</label>
            <input
              id="topic-name"
              type="text"
              value={name}
              placeholder="e.g. Photosynthesis"
              onChange={(event) => setName(event.target.value)}
            />
          </div>
          <div className="form-row">
            <label htmlFor="topic-passage">Study passage</label>
            <textarea
              id="topic-passage"
              value={passage}
              onChange={(event) => setPassage(event.target.value)}
              maxLength={passageLimit}
              placeholder="Key study notes (max 2000 characters)"
            />
            <div className="muted counter">
              {passage.length}/{passageLimit}
            </div>
          </div>
          <div className="form-actions">
            <button type="button" className="ghost-button" onClick={() => setShowForm(false)}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={isSubmitting}>
              Create Topic
            </button>
          </div>
        </form>
      )}
    </section>
  )
}
