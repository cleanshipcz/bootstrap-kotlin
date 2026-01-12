import type { ReactNode } from 'react'

interface ModalAction {
  label: string
  onClick: () => void
  variant?: 'primary' | 'secondary' | 'danger'
  disabled?: boolean
}

interface ModalProps {
  title: string
  description?: string
  children?: ReactNode
  actions: ModalAction[]
}

export function Modal({ title, description, children, actions }: ModalProps) {
  const actionClass = (variant: ModalAction['variant']) => {
    switch (variant) {
      case 'primary':
        return 'btn-primary'
      case 'danger':
        return 'btn-danger'
      default:
        return 'btn-secondary'
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal-card">
        <header>
          <h3>{title}</h3>
          {description && <p className="muted">{description}</p>}
        </header>
        {children && <div className="modal-body">{children}</div>}
        <div className="modal-actions">
          {actions.map((action) => (
            <button
              key={action.label}
              className={actionClass(action.variant)}
              onClick={action.onClick}
              disabled={action.disabled}
            >
              {action.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}

