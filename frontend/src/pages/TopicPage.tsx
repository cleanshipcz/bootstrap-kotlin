import { useEffect, useState } from 'react'
import { TopicList } from '@components/topics/TopicList'
import { topicsApi } from '@api/topics'
import { subjectsApi } from '@api/subjects'
import type { TopicDto } from '@api/types'

interface TopicPageProps {
  subjectId: number
  onSelectTopic: (topicId: number) => void
  onBack: () => void
}

const STUDY_PASSAGE_LIMIT = 2000

export function TopicPage({ subjectId, onSelectTopic, onBack }: TopicPageProps) {
  const [topics, setTopics] = useState<TopicDto[]>([])
  const [subjectName, setSubjectName] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const loadData = async () => {
    setIsLoading(true)
    try {
      const [loadedTopics, subject] = await Promise.all([
        topicsApi.getAll(subjectId),
        subjectsApi.getOne(subjectId),
      ])
      setTopics(loadedTopics)
      setSubjectName(subject.name)
    } catch (error) {
      console.error('Failed to load topics or subject:', error)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadData()
  }, [subjectId])

  const handleCreateTopic = async (name: string, studyPassage: string) => {
    try {
      await topicsApi.create(subjectId, {
        name,
        studyPassage,
      })
      // Reload topics
      const loadedTopics = await topicsApi.getAll(subjectId)
      setTopics(loadedTopics)
    } catch (error) {
      console.error('Failed to create topic:', error)
    }
  }

  return (
    <TopicList
      subjectName={subjectName}
      topics={topics}
      selectedTopicId={null}
      onSelectTopic={onSelectTopic}
      onCreateTopic={handleCreateTopic}
      passageLimit={STUDY_PASSAGE_LIMIT}
      isLoading={isLoading}
      onBack={onBack}
    />
  )
}
