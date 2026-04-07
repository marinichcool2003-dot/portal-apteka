import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import house from '../assets/static-images/Главная 1.svg'
import tasksIcon from '../assets/static-images/Задачи 1.svg'
import notifications from '../assets/static-images/Иконка уведомления.svg'
import employee from '../assets/static-images/Сотрудники 1.svg'
import news from '../assets/static-images/Значок новостей.svg'
import settings from '../assets/static-images/Настройки 1.svg'
import logo from '../assets/static-images/logo.png'

import '../styles/SecondNav.css'

function tasksSectionActive(pathname) {
    return (
        pathname === '/taskController' ||
        pathname === '/departmentTasks' ||
        pathname.startsWith('/task/') ||
        pathname.startsWith('/createTask') ||
        pathname.startsWith('/editTask')
    )
}

function isNavItemActive(pathname, item) {
    if (item.label === 'Главная') return pathname === '/dashboard'
    if (item.label === 'Новости') return pathname === '/news'
    if (item.label === 'Задачи') return tasksSectionActive(pathname)
    if (item.to === '#') return false
    return pathname === item.to
}

const navItems = [
    { to: '/dashboard', icon: house, label: 'Главная', alt: 'Главная' },
    { type: 'tasks', icon: tasksIcon, label: 'Задачи', alt: 'Задачи', hasArrow: true },
    { to: '/notifications', icon: notifications, label: 'Уведомления', alt: 'Уведомления', badge: 4 },
    { to: '/employees', icon: employee, label: 'Сотрудники', alt: 'Сотрудники' },
    { to: '/news', icon: news, label: 'Новости', alt: 'Новости' },
    { to: '#', icon: settings, label: 'Настройки', alt: 'Настройки', hasArrow: true },
]

export default function SecondNav({ isOpen, onClose }) {
    const location = useLocation()
    const [tasksOpen, setTasksOpen] = useState(() => tasksSectionActive(location.pathname))
    const [windowWidth, setWindowWidth] = useState(window.innerWidth)

    // Отслеживаем изменение размера окна
    useEffect(() => {
        const handleResize = () => {
            setWindowWidth(window.innerWidth)
        }
        window.addEventListener('resize', handleResize)
        return () => window.removeEventListener('resize', handleResize)
    }, [])

    // Автоматически закрываем сайдбар на мобильных при изменении размера
    useEffect(() => {
        if (windowWidth <= 992 && isOpen) {
            onClose()
        }
    }, [windowWidth])

    useEffect(() => {
        if (tasksSectionActive(location.pathname)) {
            setTasksOpen(true)
        }
    }, [location.pathname])

    const handleNavClick = () => {
        if (windowWidth <= 992) {
            onClose()
        }
    }

    const handleOverlayClick = () => {
        onClose()
    }

    const shouldShowOverlay = isOpen && windowWidth <= 992

    return (
        <>
            {/* Оверлей для мобильных устройств и планшетов */}
            <div
                className={`sidebar-overlay ${shouldShowOverlay ? 'visible' : ''}`}
                onClick={handleOverlayClick}
            />

            <aside className={`secondNav ${isOpen ? 'open' : 'closed'}`}>
                <div className="sidebar-project-info">
                    <img src={logo} alt="Проект" className="project-avatar" />
                    <div className="project-text">
                        <span className="project-name">Проект: IT-Отдел</span>
                        <span className="project-desc">Хз возможно какой-то текст</span>
                    </div>
                </div>

                <div className="sidebar-divider" />

                <nav>
                    <ul className="icon-links">
                        {navItems.map((item) => {
                            if (item.type === 'tasks') {
                                const isActive = tasksSectionActive(location.pathname)
                                return (
                                    <li key={item.label} className="nav-item-tasks">
                                        <div className="nav-tasks-group">
                                            <button
                                                type="button"
                                                className={`icon-square nav-tasks-toggle ${isActive ? 'active' : ''}`}
                                                onClick={() => setTasksOpen((o) => !o)}
                                                aria-expanded={tasksOpen}
                                            >
                                                <img src={item.icon} alt={item.alt} className="nav-icon" />
                                                <span className="nav-text">{item.label}</span>
                                                {item.hasArrow && (
                                                    <span className={`nav-arrow nav-arrow-rotate ${tasksOpen ? 'open' : ''}`}>
                                                        ›
                                                    </span>
                                                )}
                                            </button>
                                            {tasksOpen && (
                                                <ul className="nav-submenu">
                                                    <li>
                                                        <Link
                                                            to="/taskController"
                                                            className={`nav-submenu-link ${location.pathname === '/taskController' ? 'active' : ''}`}
                                                            onClick={handleNavClick}
                                                        >
                                                            Мои задачи
                                                        </Link>
                                                    </li>
                                                    <li>
                                                        <Link
                                                            to="/departmentTasks"
                                                            className={`nav-submenu-link ${location.pathname === '/departmentTasks' ? 'active' : ''}`}
                                                            onClick={handleNavClick}
                                                        >
                                                            Все задачи отдела
                                                        </Link>
                                                    </li>
                                                </ul>
                                            )}
                                        </div>
                                    </li>
                                )
                            }

                            const isActive = isNavItemActive(location.pathname, item)
                            return (
                                <li key={item.label}>
                                    <Link to={item.to} onClick={handleNavClick}>
                                        <div className={`icon-square ${isActive ? 'active' : ''}`}>
                                            <img src={item.icon} alt={item.alt} className="nav-icon" />
                                            <span className="nav-text">{item.label}</span>
                                            {item.badge && (
                                                <span className="nav-badge">{item.badge}</span>
                                            )}
                                            {item.hasArrow && (
                                                <span className="nav-arrow">›</span>
                                            )}
                                        </div>
                                    </Link>
                                </li>
                            )
                        })}
                    </ul>
                </nav>
            </aside>
        </>
    )
}