import { useState } from 'react'
import type { SubjectDto } from '@api/types'

interface SubjectListProps {
  subjects: SubjectDto[]
  selectedSubjectId: number | null
  onSelectSubject: (subjectId: number) => void
  onCreateSubject: (name: string) => Promise<void> | void
}

export function SubjectList({
  subjects,
  selectedSubjectId,
  onSelectSubject,
  onCreateSubject,
}: SubjectListProps) {
  const [name, setName] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    const trimmed = name.trim()
    if (!trimmed) {
      return
    }
    setIsSubmitting(true)
    await onCreateSubject(trimmed)
    setName('')
    setShowForm(false)
    setIsSubmitting(false)
  }

  return (
    <section className="subjects-view fade-in">
      <div className="view-header">
        <div>
          <p className="eyebrow">Library</p>
        <h2>Subjects</h2>
        </div>
        <button className="ghost-button" onClick={() => setShowForm(true)}>
          Add Subject
        </button>
      </div>

      <ul className="subjects-list">
        {subjects.length === 0 && <li className="muted">No subjects yet. Create your first one.</li>}
        {subjects.map((subject) => (
          <li key={subject.id ?? subject.name}>
          <button
              className={`subject-pill ${selectedSubjectId === subject.id ? 'selected' : ''}`}
            onClick={() => subject.id && onSelectSubject(subject.id)}
          >
              <span className="subject-name">{subject.name}</span>
              <span className="subject-meta">{subject.topics.length} topics</span>
          </button>
          </li>
        ))}
      </ul>

      {showForm && (
        <form className="inline-form" onSubmit={handleSubmit}>
          <label htmlFor="subject-name" className="form-label">
            Subject name
          </label>
          <div className="inline-form-row">
        <input
          id="subject-name"
          type="text"
              placeholder="e.g. Organic Chemistry"
          value={name}
          onChange={(event) => setName(event.target.value)}
              autoFocus
        />
            <div className="form-actions">
              <button type="button" className="ghost-button" onClick={() => setShowForm(false)}>
                Cancel
              </button>
              <button type="submit" className="btn-primary" disabled={isSubmitting}>
                Save
        </button>
            </div>
          </div>
      </form>
      )}
    </section>
  )
}
