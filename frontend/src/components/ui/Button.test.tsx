import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from './Button';
import { MantineProvider } from '@mantine/core';

const renderWithTheme = (ui: React.ReactNode) => {
  return render(
    <MantineProvider>
      {ui}
    </MantineProvider>
  );
};

describe('Button', () => {
  it('renders children correctly', () => {
    renderWithTheme(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('handles onClick', () => {
    const handleClick = jest.fn();
    renderWithTheme(<Button onClick={handleClick}>Click me</Button>);
    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });
});

