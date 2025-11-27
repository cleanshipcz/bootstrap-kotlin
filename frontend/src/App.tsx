import { useState, useEffect } from 'react'
import { SubjectList } from './components/SubjectList'
import { TopicList } from './components/TopicList'
import { TopicDetails } from './components/TopicDetails'
import { FlashcardList } from './components/FlashcardList'
import { ErrorToast } from './components/ErrorToast'
import { apiClient, ApiError } from './api/client'
import type { FlashcardDto, SubjectDto, TopicDto } from './api/types'
import './App.css'

const STUDY_PASSAGE_LIMIT = 2000

function App() {
  const [subjects, setSubjects] = useState<SubjectDto[]>([])
  const [topics, setTopics] = useState<TopicDto[]>([])
  const [selectedSubjectId, setSelectedSubjectId] = useState<number | null>(null)
  const [selectedTopic, setSelectedTopic] = useState<TopicDto | null>(null)
  const [flashcards, setFlashcards] = useState<FlashcardDto[]>([])
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    void loadSubjects()
  }, [])

  const loadSubjects = async (preferredSubjectId?: number | null) => {
    try {
      const loadedSubjects = await apiClient.getSubjects()
      setSubjects(loadedSubjects)
      const subjectToSelect = preferredSubjectId ?? loadedSubjects[0]?.id ?? null
      if (subjectToSelect != null) {
        await handleSelectSubject(subjectToSelect)
      } else {
        setSelectedSubjectId(null)
        setTopics([])
        clearTopicSelection()
      }
    } catch (error) {
      console.error('Failed to load subjects:', error)
    }
  }

  const loadTopics = async (subjectId: number, preferredTopicId?: number | null) => {
    try {
      const loadedTopics = await apiClient.getTopics(subjectId)
      setTopics(loadedTopics)
      const topicToSelect = preferredTopicId ?? loadedTopics[0]?.id ?? null
      if (topicToSelect != null) {
        await loadTopicDetail(topicToSelect)
      } else {
        clearTopicSelection()
      }
    } catch (error) {
      console.error('Failed to load topics:', error)
    }
  }

  const loadTopicDetail = async (topicId: number) => {
    try {
      const detail = await apiClient.getTopic(topicId)
      setSelectedTopic(detail)
      setFlashcards(detail.flashcards)
    } catch (error) {
      console.error('Failed to load topic detail:', error)
    }
  }

  const clearTopicSelection = () => {
    setSelectedTopic(null)
    setFlashcards([])
  }

  const handleSelectSubject = async (subjectId: number) => {
    setSelectedSubjectId(subjectId)
    await loadTopics(subjectId)
  }

  const handleCreateSubject = async (name: string) => {
    try {
      const created = await apiClient.createSubject({ name })
      await loadSubjects(created.id ?? null)
    } catch (error) {
      console.error('Failed to create subject:', error)
    }
  }

  const handleCreateTopic = async (name: string, studyPassage: string) => {
    if (selectedSubjectId == null) {
      return
    }
    try {
      const created = await apiClient.createTopic(selectedSubjectId, {
        name,
        studyPassage,
      })
      await loadTopics(selectedSubjectId, created.id ?? null)
    } catch (error) {
      console.error('Failed to create topic:', error)
    }
  }

  const handleSelectTopic = async (topicId: number) => {
    await loadTopicDetail(topicId)
  }

  const handleCreateFlashcard = async (question: string, answer: string) => {
    if (!selectedTopic?.id) {
      return
    }
    try {
      const newFlashcard = await apiClient.createFlashcard(selectedTopic.id, {
        question,
        answer,
      })
      setFlashcards([...flashcards, newFlashcard])
      setSelectedTopic({
        ...selectedTopic,
        flashcards: [...selectedTopic.flashcards, newFlashcard],
      })
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        setErrorMessage(error.body?.message ?? 'A flashcard with this question already exists in this topic.')
      } else {
        console.error('Failed to create flashcard:', error)
      }
    }
  }

  const handleDeleteFlashcard = async (flashcardId: number) => {
    if (!selectedTopic?.id) {
      return
    }
    try {
      await apiClient.deleteFlashcard(selectedTopic.id, flashcardId)
      setFlashcards(flashcards.filter((card) => card.id !== flashcardId))
      setSelectedTopic({
        ...selectedTopic,
        flashcards: selectedTopic.flashcards.filter((card) => card.id !== flashcardId),
      })
    } catch (error) {
      console.error('Failed to delete flashcard:', error)
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Remento</h1>
        <p className="muted">Organize subjects, topics, and flashcards</p>
      </header>
      <main className="app-main">
        <div className="grid-layout">
          <SubjectList
            subjects={subjects}
            selectedSubjectId={selectedSubjectId}
            onSelectSubject={handleSelectSubject}
            onCreateSubject={handleCreateSubject}
          />
          <TopicList
            topics={topics}
            selectedTopicId={selectedTopic?.id ?? null}
            onSelectTopic={handleSelectTopic}
            onCreateTopic={handleCreateTopic}
            passageLimit={STUDY_PASSAGE_LIMIT}
            disabled={selectedSubjectId == null}
          />
          <div className="panel-stack">
            <TopicDetails topic={selectedTopic} />
            <FlashcardList
              topicName={selectedTopic?.name ?? null}
              flashcards={flashcards}
              onCreateFlashcard={handleCreateFlashcard}
              onDeleteFlashcard={handleDeleteFlashcard}
              disabled={selectedTopic == null}
            />
          </div>
        </div>
      </main>
      {errorMessage && <ErrorToast message={errorMessage} onClose={() => setErrorMessage(null)} />}
    </div>
  )
}

export default App
