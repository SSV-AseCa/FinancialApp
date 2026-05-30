import type { AddPositionInput } from './AddPositionInput'
import type { ModifyPositionInput } from './ModifyPositionInput'
import type { Portfolio } from './Portfolio'
import type { Position } from './Position'

export interface PortfolioPort {
  fetchPortfolio(): Promise<Portfolio>
  addPosition(input: AddPositionInput): Promise<Position>
  modifyPosition(positionId: string, input: ModifyPositionInput): Promise<Position>
}
