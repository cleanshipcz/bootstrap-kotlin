import { useState } from 'react'
import type { TopicDto } from '../api/types'

interface TopicListProps {
  topics: TopicDto[]
  selectedTopicId: number | null
  onSelectTopic: (topicId: number) => void
  onCreateTopic: (name: string, studyPassage: string) => Promise<void> | void
  passageLimit: number
  disabled: boolean
}

export function TopicList({
  topics,
  selectedTopicId,
  onSelectTopic,
  onCreateTopic,
  passageLimit,
  disabled,
}: TopicListProps) {
  const [name, setName] = useState('')
  const [passage, setPassage] = useState('')

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!name.trim() || !passage.trim()) {
      return
    }
    await onCreateTopic(name.trim(), passage.trim())
    setName('')
    setPassage('')
  }

  return (
    <div className="panel">
      <header className="panel-header">
        <h2>Topics</h2>
      </header>
      <div className="list">
        {topics.length === 0 && <p className="muted">No topics for this subject.</p>}
        {topics.map((topic) => (
          <button
            key={topic.id ?? topic.name}
            className={`list-item ${selectedTopicId === topic.id ? 'active' : ''}`}
            onClick={() => topic.id && onSelectTopic(topic.id)}
          >
            {topic.name}
          </button>
        ))}
      </div>
      <form className="stack-form" onSubmit={handleSubmit}>
        <label htmlFor="topic-name">Add Topic</label>
        <input
          id="topic-name"
          type="text"
          placeholder="Topic name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          disabled={disabled}
        />
        <label htmlFor="topic-passage">Study Passage</label>
        <textarea
          id="topic-passage"
          placeholder="Key study notes (max 2000 characters)"
          value={passage}
          maxLength={passageLimit}
          onChange={(event) => setPassage(event.target.value)}
          disabled={disabled}
        />
        <div className="muted counter">
          {passage.length}/{passageLimit}
        </div>
        <button type="submit" className="btn btn-primary" disabled={disabled}>
          Create Topic
        </button>
      </form>
    </div>
  )
}

