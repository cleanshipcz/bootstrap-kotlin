import { useCallback, useEffect, useMemo, useState } from 'react'
import { SubjectList } from './components/SubjectList'
import { TopicList } from './components/TopicList'
import { TopicDetails } from './components/TopicDetails'
import { FlashcardsReadOnly } from './components/FlashcardsReadOnly'
import { FlashcardsEditor } from './components/FlashcardsEditor'
import { ErrorToast } from './components/ErrorToast'
import { apiClient } from './api/client'
import type { FlashcardDto, SubjectDto, TopicDto } from './api/types'
import { applyColorPalette } from './theme/colors'
import './App.css'

const STUDY_PASSAGE_LIMIT = 2000
type View = 'subjects' | 'topics' | 'topicDetail' | 'flashcardsRead' | 'flashcardsEdit'

function App() {
  const [subjects, setSubjects] = useState<SubjectDto[]>([])
  const [topics, setTopics] = useState<TopicDto[]>([])
  const [selectedSubjectId, setSelectedSubjectId] = useState<number | null>(null)
  const [selectedTopic, setSelectedTopic] = useState<TopicDto | null>(null)
  const [flashcards, setFlashcards] = useState<FlashcardDto[]>([])
  const [view, setView] = useState<View>('subjects')
  const [viewKey, setViewKey] = useState(0)
  const [loadingView, setLoadingView] = useState<View | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const activeSubject = useMemo(
    () => subjects.find((subject) => subject.id === selectedSubjectId) ?? null,
    [subjects, selectedSubjectId],
  )

  useEffect(() => {
    applyColorPalette()
    void loadSubjects()
  }, [])

  useEffect(() => {
    setViewKey((prev) => prev + 1)
  }, [view, selectedSubjectId, selectedTopic?.id])

  const handleError = useCallback((message: string) => {
    setErrorMessage(message)
  }, [])

  const loadSubjects = async () => {
    try {
      const loadedSubjects = await apiClient.getSubjects()
      setSubjects(loadedSubjects)
    } catch (error) {
      console.error('Failed to load subjects:', error)
    }
  }

  const loadTopics = async (subjectId: number) => {
    setLoadingView('topics')
    try {
      const loadedTopics = await apiClient.getTopics(subjectId)
      setTopics(loadedTopics)
    } catch (error) {
      console.error('Failed to load topics:', error)
    } finally {
      setLoadingView(null)
    }
  }

  const loadTopicDetail = async (topicId: number) => {
    setLoadingView('topicDetail')
    try {
      const detail = await apiClient.getTopic(topicId)
      setSelectedTopic(detail)
      setFlashcards(detail.flashcards)
    } catch (error) {
      console.error('Failed to load topic detail:', error)
    } finally {
      setLoadingView(null)
    }
  }

  const handleSelectSubject = async (subjectId: number) => {
    if (selectedSubjectId === subjectId && view === 'topics') {
      return
    }
    setSelectedSubjectId(subjectId)
    setSelectedTopic(null)
    setFlashcards([])
    setView('topics')
    await loadTopics(subjectId)
  }

  const handleBackToSubjects = () => {
    setView('subjects')
    setSelectedSubjectId(null)
    setTopics([])
    setSelectedTopic(null)
    setFlashcards([])
  }

  const handleCreateSubject = async (name: string) => {
    try {
      const created = await apiClient.createSubject({ name })
      await loadSubjects()
      if (created.id != null) {
        await handleSelectSubject(created.id)
      }
    } catch (error) {
      console.error('Failed to create subject:', error)
    }
  }

  const handleCreateTopic = async (name: string, studyPassage: string) => {
    if (selectedSubjectId == null) {
      return
    }
    try {
      await apiClient.createTopic(selectedSubjectId, {
        name,
        studyPassage,
      })
      await loadTopics(selectedSubjectId)
    } catch (error) {
      console.error('Failed to create topic:', error)
    }
  }

  const handleSelectTopic = async (topicId: number) => {
    setView('topicDetail')
    await loadTopicDetail(topicId)
  }

  const handleBackToTopics = () => {
    setView('topics')
    setSelectedTopic(null)
    setFlashcards([])
  }

  const handleFlashcardsUpdated = (updated: FlashcardDto[]) => {
    setFlashcards(updated)
    setSelectedTopic((current) => (current ? { ...current, flashcards: updated } : current))
  }

  const handleUpdateStudyPassage = async (studyPassage: string) => {
    if (!selectedTopic?.id) {
      return
    }
    try {
      const updatedTopic = await apiClient.updateTopic(selectedTopic.id, {
        name: selectedTopic.name,
        studyPassage,
      })
      setSelectedTopic(updatedTopic)
      setFlashcards(updatedTopic.flashcards)
      setTopics((prev) =>
        prev.map((topic) => (topic.id === updatedTopic.id ? { ...topic, ...updatedTopic } : topic)),
      )
    } catch (error) {
      handleError('Failed to save study notes.')
      throw error
    }
  }

  const renderView = () => {
    if (view === 'subjects') {
      return (
        <SubjectList
          subjects={subjects}
          selectedSubjectId={selectedSubjectId}
          onSelectSubject={handleSelectSubject}
          onCreateSubject={handleCreateSubject}
        />
      )
    }

    if (view === 'topics' && selectedSubjectId != null && activeSubject) {
      return (
        <TopicList
          subjectName={activeSubject.name}
          topics={topics}
          selectedTopicId={selectedTopic?.id ?? null}
          onSelectTopic={handleSelectTopic}
          onCreateTopic={handleCreateTopic}
          passageLimit={STUDY_PASSAGE_LIMIT}
          isLoading={loadingView === 'topics'}
          onBack={handleBackToSubjects}
        />
      )
    }

    if (view === 'topicDetail' && selectedTopic) {
      return (
        <TopicDetails
          topic={selectedTopic}
          onBack={handleBackToTopics}
          onViewFlashcards={() => setView('flashcardsRead')}
          isLoading={loadingView === 'topicDetail'}
          onUpdateStudyPassage={handleUpdateStudyPassage}
          onError={handleError}
        />
      )
    }

    if (view === 'flashcardsRead' && selectedTopic) {
      return (
        <FlashcardsReadOnly
          topicName={selectedTopic.name}
          flashcards={flashcards}
          onBack={() => setView('topicDetail')}
          onEdit={() => setView('flashcardsEdit')}
        />
      )
    }

    if (view === 'flashcardsEdit' && selectedTopic?.id) {
      return (
        <FlashcardsEditor
          topicId={selectedTopic.id}
          topicName={selectedTopic.name}
          flashcards={flashcards}
          onClose={() => setView('topicDetail')}
          onFlashcardsUpdated={handleFlashcardsUpdated}
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
