import { Link } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { Bell, Search, LogOut } from 'lucide-react';
import { Avatar } from '@/components/ui/Avatar';
import { ROUTES } from '@/utils/constants';
import { logout } from '@/store/slices/authSlice';
import logo from '@/assets/logo.svg';

const Navbar = () => {
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);

  return (
    <header className="fixed top-0 left-0 right-0 h-16 bg-bg-secondary/95 backdrop-blur-md border-b border-color-border z-40 flex items-center justify-between px-6">
      {/* Logo */}
      <Link to={ROUTES.DASHBOARD} className="flex items-center gap-2">
        <img src={logo} alt="WealthSense" className="h-8" />
      </Link>

      {/* Search - desktop only */}
      <div className="hidden md:flex items-center gap-2 bg-bg-primary rounded-btn border border-color-border px-3 py-2 w-80">
        <Search className="h-4 w-4 text-text-secondary" />
        <input
          type="text"
          placeholder="Search transactions, categories..."
          className="bg-transparent text-sm text-text-primary placeholder:text-text-secondary/60 w-full focus:outline-none"
        />
      </div>

      {/* Right actions */}
      <div className="flex items-center gap-3">
        {/* Notifications */}
        <button className="relative p-2 rounded-btn text-text-secondary hover:text-text-primary hover:bg-bg-primary transition-colors">
          <Bell className="h-5 w-5" />
          <span className="absolute top-1.5 right-1.5 h-2 w-2 bg-accent-green rounded-full" />
        </button>

        {/* User menu */}
        <div className="flex items-center gap-3 pl-3 border-l border-color-border">
          <div className="hidden sm:block text-right">
            <p className="text-sm font-medium text-text-primary">
              {user?.firstName || 'User'}
            </p>
            <p className="text-xs text-text-secondary">
              {user?.email || 'user@example.com'}
            </p>
          </div>
          <Avatar name={user?.firstName ? `${user.firstName} ${user.lastName || ''}` : 'User'} size="sm" />
          <button
            onClick={() => dispatch(logout())}
            className="p-2 rounded-btn text-text-secondary hover:text-accent-red hover:bg-accent-red/10 transition-colors"
            title="Logout"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </header>
  );
};

export { Navbar };
