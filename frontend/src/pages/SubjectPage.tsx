import { useEffect, useState } from 'react'
import { SubjectList } from '@components/subjects/SubjectList'
import { subjectsApi } from '@api/subjects'
import type { SubjectDto } from '@api/types'

interface SubjectPageProps {
  selectedSubjectId: number | null
  onSelectSubject: (subjectId: number) => void
}

export function SubjectPage({ selectedSubjectId, onSelectSubject }: SubjectPageProps) {
  const [subjects, setSubjects] = useState<SubjectDto[]>([])

  const loadSubjects = async () => {
    try {
      const loadedSubjects = await subjectsApi.getAll()
      setSubjects(loadedSubjects)
    } catch (error) {
      console.error('Failed to load subjects:', error)
    }
  }

  useEffect(() => {
    void loadSubjects()
  }, [])

  const handleCreateSubject = async (name: string) => {
    try {
      const created = await subjectsApi.create({ name })
      await loadSubjects()
      if (created.id != null) {
        onSelectSubject(created.id)
      }
    } catch (error) {
      console.error('Failed to create subject:', error)
    }
  }

  return (
    <SubjectList
      subjects={subjects}
      selectedSubjectId={selectedSubjectId}
      onSelectSubject={onSelectSubject}
      onCreateSubject={handleCreateSubject}
    />
  )
}

