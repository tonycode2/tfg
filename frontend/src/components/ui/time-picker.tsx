import * as React from "react"
import { Clock } from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { Input } from "@/components/ui/input"

interface TimePickerProps {
  value?: string;
  onChange: (time: string) => void;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
}

export function TimePicker({
  value,
  onChange,
  placeholder = "Seleccionar hora",
  disabled = false,
  className,
}: TimePickerProps) {
  const [hours, setHours] = React.useState<string>(value?.split(':')[0] || "08")
  const [minutes, setMinutes] = React.useState<string>(value?.split(':')[1] || "00")
  const [open, setOpen] = React.useState(false)

  React.useEffect(() => {
    if (value) {
      const [h, m] = value.split(':')
      setHours(h || "08")
      setMinutes(m || "00")
    }
  }, [value])

  const handleHoursChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value.replace(/[^0-9]/g, '').slice(0, 2)
    const numVal = parseInt(val) || 0
    const clampedVal = Math.min(Math.max(numVal, 0), 23)
    const formattedVal = clampedVal.toString().padStart(2, '0')
    setHours(formattedVal)
    onChange(`${formattedVal}:${minutes}`)
  }

  const handleMinutesChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value.replace(/[^0-9]/g, '').slice(0, 2)
    const numVal = parseInt(val) || 0
    const clampedVal = Math.min(Math.max(numVal, 0), 59)
    const formattedVal = clampedVal.toString().padStart(2, '0')
    setMinutes(formattedVal)
    onChange(`${hours}:${formattedVal}`)
  }

  const displayValue = value ? value : placeholder

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant={"outline"}
          className={cn(
            "w-full justify-start text-left font-normal",
            !value && "text-muted-foreground",
            className
          )}
          disabled={disabled}
        >
          <Clock className="mr-2 h-4 w-4" />
          <span>{displayValue}</span>
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-4">
        <div className="flex flex-col gap-4">
          <div className="text-sm font-medium text-center">Seleccionar Hora</div>
          <div className="flex items-center gap-2">
            <div className="flex flex-col items-center gap-2">
              <label className="text-xs text-muted-foreground">Hora</label>
              <Input
                type="text"
                value={hours}
                onChange={handleHoursChange}
                className="w-16 text-center text-lg"
                maxLength={2}
              />
            </div>
            <div className="text-2xl font-bold">:</div>
            <div className="flex flex-col items-center gap-2">
              <label className="text-xs text-muted-foreground">Minutos</label>
              <Input
                type="text"
                value={minutes}
                onChange={handleMinutesChange}
                className="w-16 text-center text-lg"
                maxLength={2}
              />
            </div>
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              className="flex-1"
              onClick={() => {
                setHours("08")
                setMinutes("00")
                onChange("08:00")
              }}
            >
              08:00
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="flex-1"
              onClick={() => {
                setHours("17")
                setMinutes("00")
                onChange("17:00")
              }}
            >
              17:00
            </Button>
          </div>
          <Button
            size="sm"
            onClick={() => setOpen(false)}
          >
            Confirmar
          </Button>
        </div>
      </PopoverContent>
    </Popover>
  )
}
