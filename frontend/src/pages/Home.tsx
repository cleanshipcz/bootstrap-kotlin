import { SubjectPage } from './SubjectPage'

interface HomeProps {
  onSelectSubject: (subjectId: number) => void
}

export function Home({ onSelectSubject }: HomeProps) {
  return <SubjectPage selectedSubjectId={null} onSelectSubject={onSelectSubject} />
}

