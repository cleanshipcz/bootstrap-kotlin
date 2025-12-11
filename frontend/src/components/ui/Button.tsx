import { Button as MantineButton, ButtonProps } from '@mantine/core';
import React from 'react';

interface Props extends ButtonProps {
  onClick?: () => void;
  children: React.ReactNode;
}

export const Button = ({ children, ...props }: Props) => {
  return <MantineButton {...props}>{children}</MantineButton>;
};

