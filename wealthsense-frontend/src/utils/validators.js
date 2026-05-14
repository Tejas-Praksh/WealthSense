/**
 * Validate email format
 */
export function validateEmail(email) {
  if (!email) return 'Email is required';
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  if (!emailRegex.test(email)) return 'Please enter a valid email address';
  return null;
}

/**
 * Validate Indian phone number (10 digits, starts with 6-9)
 */
export function validatePhone(phone) {
  if (!phone) return 'Phone number is required';
  const cleaned = phone.replace(/\D/g, '');
  if (cleaned.length !== 10) return 'Phone number must be 10 digits';
  if (!/^[6-9]/.test(cleaned)) return 'Invalid Indian phone number';
  return null;
}

/**
 * Validate password and return error message or null
 */
export function validatePassword(password) {
  if (!password) return 'Password is required';
  if (password.length < 8) return 'Password must be at least 8 characters';
  return null;
}

/**
 * Check password strength: WEAK, MEDIUM, STRONG
 */
export function getPasswordStrength(password) {
  if (!password) return null;

  let score = 0;
  if (password.length >= 8) score++;
  if (password.length >= 12) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[a-z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;

  if (score <= 2) return 'WEAK';
  if (score <= 4) return 'MEDIUM';
  return 'STRONG';
}

/**
 * Validate required field
 */
export function validateRequired(value, fieldName = 'This field') {
  if (!value || (typeof value === 'string' && !value.trim())) {
    return `${fieldName} is required`;
  }
  return null;
}

/**
 * Validate password match
 */
export function validatePasswordMatch(password, confirmPassword) {
  if (!confirmPassword) return 'Please confirm your password';
  if (password !== confirmPassword) return 'Passwords do not match';
  return null;
}

/**
 * Validate name (2-50 chars, letters only)
 */
export function validateName(name, fieldName = 'Name') {
  if (!name || !name.trim()) return `${fieldName} is required`;
  if (name.trim().length < 2) return `${fieldName} must be at least 2 characters`;
  if (name.trim().length > 50) return `${fieldName} must be under 50 characters`;
  return null;
}
