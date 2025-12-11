import { useEffect, useState } from 'react'
import { TopicDetails } from '@components/topics/TopicDetails'
import { FlashcardsReadOnly } from '@components/flashcards/FlashcardsReadOnly'
import { FlashcardsEditor } from '@components/flashcards/FlashcardsEditor'
import { topicsApi } from '@api/topics'
import type { FlashcardDto, TopicDto } from '@api/types'

interface FlashcardPageProps {
  topicId: number
  onBack: () => void
  onError: (message: string) => void
}

type ViewState = 'detail' | 'read' | 'edit'

export function FlashcardPage({ topicId, onBack, onError }: FlashcardPageProps) {
  const [view, setView] = useState<ViewState>('detail')
  const [topic, setTopic] = useState<TopicDto | null>(null)
  const [flashcards, setFlashcards] = useState<FlashcardDto[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const loadTopicDetail = async () => {
    setIsLoading(true)
    try {
      const detail = await topicsApi.getOne(topicId)
      setTopic(detail)
      setFlashcards(detail.flashcards)
    } catch (error) {
      console.error('Failed to load topic detail:', error)
      onError('Failed to load topic details')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadTopicDetail()
  }, [topicId])

  const handleUpdateStudyPassage = async (studyPassage: string) => {
    if (!topic?.id) return
    try {
      const updatedTopic = await topicsApi.update(topic.id, {
        name: topic.name,
        studyPassage,
      })
      setTopic(updatedTopic)
      setFlashcards(updatedTopic.flashcards)
    } catch (error) {
      onError('Failed to save study notes.')
      throw error
    }
  }

  const handleFlashcardsUpdated = (updated: FlashcardDto[]) => {
    setFlashcards(updated)
    setTopic((current) => (current ? { ...current, flashcards: updated } : current))
  }

  if (!topic && isLoading) {
    return <div>Loading...</div> // Or a skeleton
  }

  if (!topic) {
    return <div>Topic not found</div>
  }

  if (view === 'read') {
    return (
      <FlashcardsReadOnly
        topicName={topic.name}
        flashcards={flashcards}
        onBack={() => setView('detail')}
        onEdit={() => setView('edit')}
      />
    )
  }

  if (view === 'edit') {
    return (
      <FlashcardsEditor
        topicId={topic.id!}
        topicName={topic.name}
        flashcards={flashcards}
        onClose={() => setView('detail')}
        onFlashcardsUpdated={handleFlashcardsUpdated}
        onError={onError}
      />
    )
  }

  return (
    <TopicDetails
      topic={topic}
      onBack={onBack}
      onViewFlashcards={() => setView('read')}
      isLoading={isLoading}
      onUpdateStudyPassage={handleUpdateStudyPassage}
      onError={onError}
    />
  )
}

