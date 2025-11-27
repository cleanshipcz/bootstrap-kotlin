import { useState } from 'react'
import type { SubjectDto } from '../api/types'

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

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!name.trim()) {
      return
    }
    await onCreateSubject(name.trim())
    setName('')
  }

  return (
    <div className="panel">
      <header className="panel-header">
        <h2>Subjects</h2>
      </header>
      <div className="list">
        {subjects.length === 0 && <p className="muted">No subjects yet.</p>}
        {subjects.map((subject) => (
          <button
            key={subject.id ?? subject.name}
            className={`list-item ${selectedSubjectId === subject.id ? 'active' : ''}`}
            onClick={() => subject.id && onSelectSubject(subject.id)}
          >
            {subject.name}
            {subject.topics.length > 0 && <span className="badge">{subject.topics.length}</span>}
          </button>
        ))}
      </div>
      <form className="stack-form" onSubmit={handleSubmit}>
        <label htmlFor="subject-name">Add Subject</label>
        <input
          id="subject-name"
          type="text"
          placeholder="Subject name"
          value={name}
          onChange={(event) => setName(event.target.value)}
        />
        <button type="submit" className="btn btn-primary">
          Create Subject
        </button>
      </form>
    </div>
  )
}

