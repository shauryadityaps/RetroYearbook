import React from 'react';
import { Loader2 } from 'lucide-react';

interface LeatherButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  text: string;
  icon?: React.ReactNode;
  isLoading?: boolean;
  variant?: 'primary' | 'secondary' | 'gold' | 'danger';
}

export const LeatherButton: React.FC<LeatherButtonProps> = ({
  text,
  icon,
  isLoading = false,
  variant = 'primary',
  disabled,
  className = '',
  ...props
}) => {
  let bgStyle = 'linear-gradient(180deg, #6A2F0F 0%, #471D06 100%)';
  let borderStyle = '1.5px solid #D4AF37';
  let textColor = '#FFF';

  if (variant === 'gold') {
    bgStyle = 'linear-gradient(180deg, #D4AF37 0%, #997A15 100%)';
    borderStyle = '1.5px solid #F3E5AB';
    textColor = '#2B1810';
  } else if (variant === 'danger') {
    bgStyle = 'linear-gradient(180deg, #A82424 0%, #661010 100%)';
    borderStyle = '1.5px solid #E57373';
  } else if (variant === 'secondary') {
    bgStyle = 'linear-gradient(180deg, #EDE4D0 0%, #D9CDB0 100%)';
    borderStyle = '1.5px solid #C4B49A';
    textColor = '#2B1810';
  }

  return (
    <button
      disabled={disabled || isLoading}
      className={`btn-leather ${className}`}
      style={{
        background: bgStyle,
        border: borderStyle,
        color: textColor,
        padding: '12px 20px',
        fontSize: '13px',
        width: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '8px',
        opacity: disabled || isLoading ? 0.6 : 1
      }}
      {...props}
    >
      {isLoading ? (
        <Loader2 className="animate-spin" size={16} />
      ) : (
        icon
      )}
      <span>{text}</span>
    </button>
  );
};
