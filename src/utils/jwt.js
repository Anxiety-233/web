export function decodeJwt(token) {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
    return decoded;
  } catch {
    return null;
  }
}

export function getRoleFromToken(token) {
  const payload = decodeJwt(token);
  if (!payload || !payload.roles) return 'user';
  const roles = payload.roles;
  if (Array.isArray(roles)) {
    const hasAdmin = roles.some(r =>
      (typeof r === 'string' && r.includes('ADMIN')) ||
      (typeof r === 'object' && r.authority && r.authority.includes('ADMIN'))
    );
    return hasAdmin ? 'admin' : 'user';
  }
  return 'user';
}
