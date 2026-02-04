import api from './hooks/axios.js'
import { useState, useEffect } from "react";

export function useTasksByGroup(groupId) {
    const [tasks, setTasks] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        const fetchTasks = async () => {
            if(!groupId) return;
            try {
                const response = await api.get(`/task/by-group/${groupId}`)
                console.log(response.data)
                setTasks(response.data)
            } catch (err) {
                setError(err.message || "Произошла ошибка при загрузке аптек")
            } finally {
                setLoading(false)
            }
        }
        fetchTasks()
    }, [groupId])

    return { tasks, loading, error }
}