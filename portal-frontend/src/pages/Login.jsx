import { useState } from 'react'
import logo from '../assets/static-images/logo.png'
import '../styles/login/Login.css'
import { useLogin } from '../hooks/Login' 
import { useNavigate } from 'react-router-dom'

export default function Login() {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [rememberMe, setRememberMe] = useState(false)
    const { login, loading, error } = useLogin()
    const navigate = useNavigate()

    const handleSubmit = async (e) => {
        e.preventDefault()
        
        if (!username.trim() || !password.trim()) {
            return
        }

        const result = await login(username, password, rememberMe)
        
        if (result.success) {
            navigate('/taskController') 
        }
    }

    return (
        <div className='login-body'>
            <div className='login__container'>
                <div className='login-header'>
                    <img src={logo} alt="Логотип Социальных аптек" />
                    <h1>Социальная аптека</h1>
                    <span>Вход в систему</span>
                </div>
                <form className='login-form' onSubmit={handleSubmit}>
                    <div className='input__container'>
                        <label htmlFor="username">Логин</label>
                        <input 
                            type="text" 
                            id="username"
                            placeholder='Введите логин' 
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            disabled={loading}
                            required
                        />
                    </div>
                    <div className='input__container'>
                        <label htmlFor="password">Пароль</label>
                        <input 
                            type="password" 
                            id="password"
                            placeholder='Введите пароль' 
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            disabled={loading}
                            required
                        />
                    </div>
                    <div className='remember-me__container'>
                        <label htmlFor="remember-check">Запомнить меня</label>
                        <input 
                            type="checkbox" 
                            name="remember-check" 
                            className="apple-check" 
                            id="remember-check"
                            checked={rememberMe}
                            onChange={(e) => setRememberMe(e.target.checked)}
                            disabled={loading}
                        />
                    </div>
                    
                    {error && (
                        <div className="error-message" style={{color: 'red', marginBottom: '10px'}}>
                            {error}
                        </div>
                    )}
                    
                    <button 
                        type="submit" 
                        className="login-button"
                        disabled={loading || !username.trim() || !password.trim()}
                    >
                        <span>{loading ? 'Вход...' : 'Войти'}</span>
                    </button>
                </form>
            </div>
        </div>
    )
}