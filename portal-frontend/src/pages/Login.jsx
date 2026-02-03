import logo from '../assets/static-images/logo.png'

import '../styles/login/Login.css'

export default function Login() {
    return (
        <div className='login-body'>
            <div className='login__container'>
                <div className='login-header'>
                    <img src={logo} alt="Логотип Социальных аптек" />
                    <h1>Социальная аптека</h1>
                    <span>Вход в систему</span>
                </div>
                <form className='login-form' action="submit">
                    <div className='input__container'>
                        <label htmlFor="username">Логин</label>
                        <input type="username" placeholder='Введите логин' />
                    </div>
                    <div className='input__container'>
                        <label htmlFor="password">Пароль</label>
                        <input type="password" placeholder='Введите пароль' />
                    </div>
                    <div className='remember-me__container'>
                        <label>Запомнить меня</label>
                        <input type="checkbox" name="remember-check" className="apple-check" id="remember-check" placeholder='Ф'/>
                    </div>
                    <button type="submit">Войти</button>
                </form>
            </div>
        </div>
    )
}