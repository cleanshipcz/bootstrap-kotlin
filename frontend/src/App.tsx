import { useEffect, useState } from 'react'
import { Home } from '@pages/Home'
import { TopicPage } from '@pages/TopicPage'
import { FlashcardPage } from '@pages/FlashcardPage'
import { ErrorToast } from '@components/ui/ErrorToast'
import { applyColorPalette } from '@theme/colors'
import './App.css'

type View = 'subjects' | 'topics' | 'flashcards'

function App() {
  const [view, setView] = useState<View>('subjects')
  const [selectedSubjectId, setSelectedSubjectId] = useState<number | null>(null)
  const [selectedTopicId, setSelectedTopicId] = useState<number | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [viewKey, setViewKey] = useState(0)

  useEffect(() => {
    applyColorPalette()
  }, [])

  useEffect(() => {
    setViewKey((prev) => prev + 1)
  }, [view, selectedSubjectId, selectedTopicId])

  const handleSelectSubject = (subjectId: number) => {
    setSelectedSubjectId(subjectId)
    setView('topics')
  }

  const handleSelectTopic = (topicId: number) => {
    setSelectedTopicId(topicId)
    setView('flashcards')
  }

  const handleBackToSubjects = () => {
    setSelectedSubjectId(null)
    setView('subjects')
  }

  const handleBackToTopics = () => {
    setSelectedTopicId(null)
    setView('topics')
  }

  const handleError = (message: string) => {
    setErrorMessage(message)
  }

  const renderView = () => {
    if (view === 'subjects') {
      return <Home onSelectSubject={handleSelectSubject} />
    }

    if (view === 'topics' && selectedSubjectId != null) {
      return (
        <TopicPage
          subjectId={selectedSubjectId}
          onSelectTopic={handleSelectTopic}
          onBack={handleBackToSubjects}
        />
      )
    }

    if (view === 'flashcards' && selectedTopicId != null) {
      return (
        <FlashcardPage
          topicId={selectedTopicId}
          onBack={handleBackToTopics}
          onError={handleError}
        />
      )
    }

    return (
      <div className="empty-view">
        <p>Select a subject to get started.</p>
      </div>
    )
  }

  return (
    <div className="app">
      <header className="app-hero">
        <div>
          <h1>Remento</h1>
          <p className="muted">Learn and retain.</p>
        </div>
      </header>
      <main className="app-main">
        <div key={viewKey} className={`view-surface view-${view}`}>
          {renderView()}
        </div>
      </main>
      {errorMessage && <ErrorToast message={errorMessage} onClose={() => setErrorMessage(null)} />}
    </div>
  )
}

export default App
