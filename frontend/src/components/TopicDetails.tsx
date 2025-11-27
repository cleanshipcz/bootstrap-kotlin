import type { TopicDto } from '../api/types'

interface TopicDetailsProps {
  topic: TopicDto | null
}

export function TopicDetails({ topic }: TopicDetailsProps) {
  if (!topic) {
    return (
      <div className="panel">
        <header className="panel-header">
          <h2>Topic Details</h2>
        </header>
        <p className="muted">Select a topic to view study material.</p>
      </div>
    )
  }

  return (
    <div className="panel">
      <header className="panel-header">
        <div>
          <h2>{topic.name}</h2>
          <p className="muted">Study passage</p>
        </div>
      </header>
      <div className="study-passage">{topic.studyPassage || 'No study passage provided yet.'}</div>
    </div>
  )
}

